package com.ythub.core;

import com.ythub.rendering.DirectVideoRenderer;
import com.ythub.rendering.VideoEncoder;
import com.ythub.voice.TTSEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class VideoGenerator {

    private static final Logger logger = LoggerFactory.getLogger(VideoGenerator.class);

    private final VideoConfig config;
    private final DirectVideoRenderer directRenderer;
    private final VideoEncoder videoEncoder;
    private final TTSEngine ttsEngine;

    public VideoGenerator(VideoConfig config) {
        this.config = config;
        this.directRenderer = new DirectVideoRenderer(config);
        this.videoEncoder = new VideoEncoder(config);
        this.ttsEngine = new TTSEngine(config);
    }

    public VideoGenerator() {
        this(new VideoConfig());
    }

    public File generateVideo(Timeline timeline, String outputFileName) {
        try {
            logger.info("Starting video generation: {}", timeline.getVideoTitle());
            logger.info(config.toString());

            ensureDirectories();

            String videoPath = config.getOutputDir() + "/" + outputFileName;
            String tempVideoPath = config.getTempDir() + "/temp_video.mp4";
            String tempAudioPath = config.getTempDir() + "/temp_audio.mp3";

            logger.info("Generating audio from narration...");
            generateAudio(timeline, tempAudioPath);

            logger.info("Rendering video directly (no intermediate frames)...");
            directRenderer.renderTimelineDirectly(timeline, tempVideoPath);

            logger.info("Merging audio and video...");
            videoEncoder.mergeAudioVideo(tempVideoPath, tempAudioPath, videoPath);

            logger.info("Cleaning up temporary files...");
            cleanup(tempVideoPath, tempAudioPath);

            logger.info("Video generation complete: {}", videoPath);

            return new File(videoPath);

        } catch (Exception e) {
            logger.error("Error generating video", e);
            throw new RuntimeException("Video generation failed", e);
        }
    }

    private void ensureDirectories() {
        new File(config.getOutputDir()).mkdirs();
        new File(config.getTempDir()).mkdirs();
    }

    private void generateAudio(Timeline timeline, String outputPath) {
        ttsEngine.generateTimelinedAudio(timeline, outputPath);
    }

    private void cleanup(String... tempFiles) {
        for (String tempFile : tempFiles) {
            new File(tempFile).delete();
        }
    }

    public VideoConfig getConfig() {
        return config;
    }
}
