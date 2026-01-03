package com.ythub.voice;

import com.ythub.core.Scene;
import com.ythub.core.Timeline;
import com.ythub.core.VideoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;



public class TTSEngine {

    private static final Logger logger = LoggerFactory.getLogger(TTSEngine.class);

    private final VideoConfig config;
    private final TTSProvider ttsProvider;

    public TTSEngine(VideoConfig config) {
        this.config = config;
        this.ttsProvider = createTTSProvider();
        logger.info("TTS Engine initialized using provider: {}", ttsProvider.getProviderName());
    }

    private TTSProvider createTTSProvider() {
        String provider = config.getTtsProvider().toLowerCase();

        switch (provider) {
            case "google":
            case "gtts":
                logger.info("Using Google Text-to-Speech (FREE - no API key needed, male voice)");
                return new GoogleTTSProvider(config);
            case "silent":
            default:
                logger.info("Using silent audio placeholder");
                return new SilentTTSProvider(config.getAudioSampleRate());
        }
    }

    public void generateTimelinedAudio(Timeline timeline, String outputPath) {
        try {
            List<AudioSegment> segments = new ArrayList<>();

            logger.info("Generating DYNAMIC audio for {} scenes in parallel", timeline.getScenes().size());

            ExecutorService audioPool = Executors.newFixedThreadPool(
                Math.min(timeline.getScenes().size(), Runtime.getRuntime().availableProcessors() * 2)
            );

            List<Future<SceneAudioPair>> futures = new ArrayList<>();

            for (Scene scene : timeline.getScenes()) {
                if (scene.getNarrationText() != null && !scene.getNarrationText().isEmpty()) {
                    Future<SceneAudioPair> future = audioPool.submit(() -> {
                        try {
                            byte[] audioData = generateAudio(scene.getNarrationText());
                            if (audioData == null || audioData.length == 0) {
                                logger.warn("Empty audio for scene {}, using silence", scene.getId());
                                return null;
                            }
                            logger.info("Audio generated for scene: {}", scene.getId());
                            return new SceneAudioPair(scene, audioData);
                        } catch (IOException e) {
                            logger.error("Failed to generate audio for scene {}: {}", scene.getId(), e.getMessage());
                            return null;
                        }
                    });
                    futures.add(future);
                } else {
                    futures.add(null);
                }
            }

            File tempDir = new File(config.getTempDir() + "/audio_measure");
            tempDir.mkdirs();

            int sceneIndex = 0;
            for (Future<SceneAudioPair> future : futures) {
                if (future != null) {
                    SceneAudioPair pair = future.get();
                    if (pair != null) {
                        String tempFile = tempDir.getAbsolutePath() + "/temp_" + sceneIndex + ".mp3";
                        java.nio.file.Files.write(java.nio.file.Paths.get(tempFile), pair.audioData);

                        double actualDuration = getAudioDuration(tempFile);
                        if (actualDuration > 0) {
                            logger.info("Scene {} DYNAMIC duration: original={}s, actual audio={}s",
                                pair.scene.getId(), pair.scene.getDuration(), actualDuration);
                            pair.scene.setDuration(actualDuration);
                        }

                        segments.add(new AudioSegment(pair.audioData, pair.scene.getDuration()));
                        sceneIndex++;
                    }
                }
            }

            deleteDirectory(tempDir);

            timeline.recalculateTimings();

            audioPool.shutdown();
            audioPool.awaitTermination(5, TimeUnit.MINUTES);

            if (ttsProvider instanceof GoogleTTSProvider) {
                mergeMP3Segments(segments, outputPath, timeline.getTotalDuration());
            } else {
                mergeAudioSegments(segments, outputPath, timeline.getTotalDuration());
            }

        } catch (Exception e) {
            logger.error("Error generating timeline audio", e);
            createSilence(outputPath, timeline.getTotalDuration());
        }
    }

    public byte[] generateAudio(String text) throws IOException {
        return ttsProvider.generateAudio(text);
    }

    private byte[] generateSilence(int durationMs) {
        int sampleRate = config.getAudioSampleRate();
        int numSamples = (sampleRate * durationMs) / 1000;
        byte[] silence = new byte[numSamples * 2];
        return silence;
    }

    private void mergeMP3Segments(List<AudioSegment> segments, String outputPath, double totalDuration) {
        File tempDir = null;
        try {
            if (segments.isEmpty()) {
                logger.warn("No audio segments to merge, creating silence");
                createSilence(outputPath, totalDuration);
                return;
            }

            tempDir = new File(config.getTempDir() + "/audio_segments");
            tempDir.mkdirs();

            List<String> processedFiles = new ArrayList<>();

            for (int i = 0; i < segments.size(); i++) {
                AudioSegment segment = segments.get(i);
                String rawFile = tempDir.getAbsolutePath() + "/raw_" + i + ".mp3";
                String processedFile = tempDir.getAbsolutePath() + "/processed_" + i + ".mp3";

                java.nio.file.Files.write(java.nio.file.Paths.get(rawFile), segment.data);

                double actualDuration = getAudioDuration(rawFile);

                if (actualDuration > 0 && Math.abs(actualDuration - segment.duration) > 0.1) {
                    double speedFactor = actualDuration / segment.duration;
                    speedFactor = Math.max(0.5, Math.min(2.0, speedFactor));

                    logger.info("Time-stretching segment {} from {}s to {}s (factor: {:.2f})",
                        i, actualDuration, segment.duration, speedFactor);

                    ProcessBuilder stretchPb = new ProcessBuilder(
                            "ffmpeg",
                            "-y",
                            "-i", rawFile,
                            "-af", String.format("atempo=%.4f,apad=whole_dur=%.3f", speedFactor, segment.duration),
                            "-c:a", "libmp3lame",
                            "-b:a", config.getAudioBitrate() + "k",
                            processedFile
                    );

                    stretchPb.redirectError(ProcessBuilder.Redirect.DISCARD);
                    Process stretchProcess = stretchPb.start();
                    int stretchExit = stretchProcess.waitFor();

                    if (stretchExit == 0) {
                        processedFiles.add(processedFile);
                    } else {
                        logger.warn("Failed to time-stretch segment {}, using original", i);
                        processedFiles.add(rawFile);
                    }
                } else {
                    ProcessBuilder padPb = new ProcessBuilder(
                            "ffmpeg",
                            "-y",
                            "-i", rawFile,
                            "-af", String.format("apad=whole_dur=%.3f", segment.duration),
                            "-c:a", "libmp3lame",
                            "-b:a", config.getAudioBitrate() + "k",
                            processedFile
                    );

                    padPb.redirectError(ProcessBuilder.Redirect.DISCARD);
                    Process padProcess = padPb.start();
                    int padExit = padProcess.waitFor();

                    if (padExit == 0) {
                        processedFiles.add(processedFile);
                    } else {
                        processedFiles.add(rawFile);
                    }
                }
            }

            String concatListPath = tempDir.getAbsolutePath() + "/concat_list.txt";
            StringBuilder concatList = new StringBuilder();
            for (String file : processedFiles) {
                concatList.append("file '").append(file).append("'\n");
            }
            java.nio.file.Files.write(java.nio.file.Paths.get(concatListPath), concatList.toString().getBytes());

            File outputFile = new File(outputPath);
            outputFile.getParentFile().mkdirs();

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-y",
                    "-f", "concat",
                    "-safe", "0",
                    "-i", concatListPath,
                    "-c:a", "libmp3lame",
                    "-b:a", config.getAudioBitrate() + "k",
                    outputPath
            );

            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg audio merge failed with exit code: " + exitCode);
            }

            logger.info("Time-aligned MP3 audio merged successfully: {} (duration: {}s)", outputPath, totalDuration);

        } catch (Exception e) {
            logger.error("Error merging MP3 segments", e);
            createSilence(outputPath, totalDuration);
        } finally {
            if (tempDir != null && tempDir.exists()) {
                deleteDirectory(tempDir);
            }
        }
    }

    private double getAudioDuration(String audioFile) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe",
                    "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    audioFile
            );

            Process process = pb.start();
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()));
            String durationStr = reader.readLine();
            process.waitFor();

            if (durationStr != null && !durationStr.isEmpty()) {
                return Double.parseDouble(durationStr);
            }
        } catch (Exception e) {
            logger.warn("Failed to get audio duration for {}: {}", audioFile, e.getMessage());
        }
        return 0;
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    private void mergeAudioSegments(List<AudioSegment> segments, String outputPath, double totalDuration) {
        try {
            AudioFormat format = new AudioFormat(
                    config.getAudioSampleRate(),
                    16,
                    config.getAudioChannels(),
                    true,
                    false
            );

            int totalSamples = (int) (config.getAudioSampleRate() * totalDuration);
            byte[] finalAudio = new byte[totalSamples * 2 * config.getAudioChannels()];

            int currentPosition = 0;
            for (AudioSegment segment : segments) {
                int segmentLength = Math.min(segment.data.length, finalAudio.length - currentPosition);
                System.arraycopy(segment.data, 0, finalAudio, currentPosition, segmentLength);
                currentPosition += (int) (config.getAudioSampleRate() * segment.duration * 2 * config.getAudioChannels());
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(finalAudio);
            AudioInputStream audioInputStream = new AudioInputStream(bais, format, finalAudio.length / format.getFrameSize());

            File outputFile = new File(outputPath);
            outputFile.getParentFile().mkdirs();
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, outputFile);

            logger.info("Audio file created: {}", outputPath);

        } catch (Exception e) {
            logger.error("Error merging audio segments", e);
            createSilence(outputPath, totalDuration);
        }
    }

    private void createSilence(String outputPath, double duration) {
        try {
            AudioFormat format = new AudioFormat(
                    config.getAudioSampleRate(),
                    16,
                    config.getAudioChannels(),
                    true,
                    false
            );

            int numSamples = (int) (config.getAudioSampleRate() * duration);
            byte[] silence = new byte[numSamples * 2 * config.getAudioChannels()];

            ByteArrayInputStream bais = new ByteArrayInputStream(silence);
            AudioInputStream audioInputStream = new AudioInputStream(bais, format, silence.length / format.getFrameSize());

            File outputFile = new File(outputPath);
            outputFile.getParentFile().mkdirs();
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, outputFile);

            logger.info("Silent audio file created: {}", outputPath);

        } catch (Exception e) {
            logger.error("Error creating silence", e);
        }
    }

    private static class AudioSegment {
        byte[] data;
        double duration;

        AudioSegment(byte[] data, double duration) {
            this.data = data;
            this.duration = duration;
        }
    }

    private static class SceneAudioPair {
        Scene scene;
        byte[] audioData;

        SceneAudioPair(Scene scene, byte[] audioData) {
            this.scene = scene;
            this.audioData = audioData;
        }
    }
}
