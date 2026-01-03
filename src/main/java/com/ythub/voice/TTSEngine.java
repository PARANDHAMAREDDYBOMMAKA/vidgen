package com.ythub.voice;

import com.ythub.core.Scene;
import com.ythub.core.Timeline;
import com.ythub.core.VideoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TTSEngine {

    private static final Logger logger = LoggerFactory.getLogger(TTSEngine.class);

    private final VideoConfig config;

    public TTSEngine(VideoConfig config) {
        this.config = config;
        logger.info("TTS Engine initialized (using silent audio - you can add voiceover later)");
    }

    public void generateTimelinedAudio(Timeline timeline, String outputPath) {
        try {
            List<AudioSegment> segments = new ArrayList<>();

            for (Scene scene : timeline.getScenes()) {
                if (scene.getNarrationText() != null && !scene.getNarrationText().isEmpty()) {
                    byte[] audioData = generateAudio(scene.getNarrationText());
                    double duration = scene.getDuration();
                    segments.add(new AudioSegment(audioData, duration));
                }
            }

            mergeAudioSegments(segments, outputPath, timeline.getTotalDuration());

        } catch (Exception e) {
            logger.error("Error generating timeline audio", e);
            createSilence(outputPath, timeline.getTotalDuration());
        }
    }

    public byte[] generateAudio(String text) {
        return generateSilence(text.length() * 50);
    }

    private byte[] generateSilence(int durationMs) {
        int sampleRate = config.getAudioSampleRate();
        int numSamples = (sampleRate * durationMs) / 1000;
        byte[] silence = new byte[numSamples * 2];
        return silence;
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
}
