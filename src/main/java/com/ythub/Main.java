package com.ythub;

import com.ythub.animation.AnimationEngine;
import com.ythub.core.Timeline;
import com.ythub.core.VideoConfig;
import com.ythub.core.VideoGenerator;
import com.ythub.dsa.algorithms.BubbleSortVisualizerEnhanced;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== YTHub Video Generator ===");
        System.out.println("Starting video generation...\n");

        VideoConfig config;

        if (args.length > 0 && args[0].equals("--1080p")) {
            config = new VideoConfig(VideoConfig.Resolution.HD_1080P_60);
        } else if (args.length > 0 && args[0].equals("--fast")) {
            config = new VideoConfig(VideoConfig.Resolution.HD_1080P_30);
        } else {
            config = new VideoConfig(VideoConfig.Resolution.UHD_4K);
        }

        System.out.println("Using FREE Google Text-to-Speech (no API key needed)");
        System.out.println("Configuration: " + config);

        VideoGenerator generator = new VideoGenerator(config);

        System.out.println("\nGenerating Comprehensive Bubble Sort video (10-15 minutes)...");
        generateEnhancedBubbleSortVideo(generator, config);

        System.out.println("\n=== Video Generation Complete ===");

        Platform.exit();
    }

    private static void generateEnhancedBubbleSortVideo(VideoGenerator generator, VideoConfig config) {
        Canvas canvas = new Canvas(config.getWidth(), config.getHeight());
        GraphicsContext gc = canvas.getGraphicsContext2D();
        AnimationEngine animationEngine = new AnimationEngine(gc, config.getWidth(), config.getHeight());

        BubbleSortVisualizerEnhanced visualizer = new BubbleSortVisualizerEnhanced(config, animationEngine);
        Timeline timeline = visualizer.createComprehensiveBubbleSortVideo();

        timeline.printSummary();

        String filename = "bubble-sort-complete-" + config.getResolution().name + ".mp4";
        File outputFile = generator.generateVideo(timeline, filename);
        System.out.println("\nVideo generated: " + outputFile.getAbsolutePath());
        System.out.println("Duration: " + timeline.getFormattedDuration() + " minutes");
        System.out.println("Total frames: " + (int)(timeline.getTotalDuration() * config.getFps()));
    }
}
