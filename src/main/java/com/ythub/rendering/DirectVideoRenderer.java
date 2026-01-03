package com.ythub.rendering;

import com.ythub.core.Scene;
import com.ythub.core.Timeline;
import com.ythub.core.VideoConfig;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class DirectVideoRenderer {

    private static final Logger logger = LoggerFactory.getLogger(DirectVideoRenderer.class);

    private final VideoConfig config;
    private Canvas canvas;
    private GraphicsContext gc;
    private static boolean javaFXInitialized = false;
    private final Object canvasLock = new Object();

    public DirectVideoRenderer(VideoConfig config) {
        this.config = config;

        if (!javaFXInitialized) {
            initJavaFX();
            javaFXInitialized = true;
        }

        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            canvas = new Canvas(config.getWidth(), config.getHeight());
            gc = canvas.getGraphicsContext2D();
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void initJavaFX() {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    public void renderTimelineDirectly(Timeline timeline, String outputPath) {
        ExecutorService renderPool = null;
        FFmpegFrameRecorder tempRecorder = null;

        try {
            tempRecorder = new FFmpegFrameRecorder(outputPath, config.getWidth(), config.getHeight());
            final FFmpegFrameRecorder recorder = tempRecorder;
            final Java2DFrameConverter converter = new Java2DFrameConverter();
            recorder.setFrameRate(config.getFps());
            recorder.setVideoBitrate(config.getBitrate() * 1000);

            if ("hevc".equalsIgnoreCase(config.getCodec())) {
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_HEVC);
                recorder.setVideoOption("preset", "medium");
                recorder.setVideoOption("tune", "animation");
            } else {
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setVideoOption("preset", "medium");
                recorder.setVideoOption("tune", "animation");
            }

            recorder.setVideoOption("g", String.valueOf(config.getFps()));
            recorder.setVideoOption("keyint_min", String.valueOf(config.getFps()));

            recorder.setFormat(config.getFormat());
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);

            logger.info("Starting video encoder with ultrafast preset for maximum performance");
            recorder.start();

            double totalDuration = timeline.getTotalDuration();
            int totalFrames = (int) (totalDuration * config.getFps());

            int numThreads = Runtime.getRuntime().availableProcessors() * 4;
            int batchSize = Math.max(config.getFps() * 2, 120);

            logger.info("ULTRA-FAST MODE: Rendering {} frames with {} threads in batches of {} frames",
                totalFrames, numThreads, batchSize);
            logger.info("Target: 10-minute video in under 1 minute");

            renderPool = Executors.newFixedThreadPool(numThreads);
            AtomicInteger framesRendered = new AtomicInteger(0);
            AtomicInteger framesWritten = new AtomicInteger(0);
            ConcurrentHashMap<Integer, BufferedImage> frameBuffer = new ConcurrentHashMap<>();
            AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

            Thread writerThread = new Thread(() -> {
                try {
                    while (framesWritten.get() < totalFrames) {
                        int nextFrame = framesWritten.get();
                        BufferedImage frameImage = frameBuffer.remove(nextFrame);

                        if (frameImage != null) {
                            Frame frame = converter.convert(frameImage);
                            recorder.record(frame);
                            framesWritten.incrementAndGet();

                            if (framesWritten.get() % (config.getFps() * 3) == 0) {
                                int secondsRendered = framesWritten.get() / config.getFps();
                                int totalSeconds = (int) totalDuration;
                                double speedMultiplier = (double) framesWritten.get() /
                                    (System.currentTimeMillis() - startTime.get()) * 1000 / config.getFps();
                                logger.info("Progress: {}/{} seconds ({}%) - Speed: {:.1f}x realtime",
                                    secondsRendered, totalSeconds,
                                    (framesWritten.get() * 100 / totalFrames), speedMultiplier);
                            }
                        } else {
                            Thread.sleep(1);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Writer thread error", e);
                }
            });

            writerThread.start();

            for (int batchStart = 0; batchStart < totalFrames; batchStart += batchSize) {
                int batchEnd = Math.min(batchStart + batchSize, totalFrames);
                List<Future<?>> batchFutures = new ArrayList<>();

                for (int frameIndex = batchStart; frameIndex < batchEnd; frameIndex++) {
                    final int finalFrameIndex = frameIndex;
                    Future<?> future = renderPool.submit(() -> {
                        try {
                            double currentTime = (double) finalFrameIndex / config.getFps();
                            Scene currentScene = timeline.getSceneAtTime(currentTime);

                            if (currentScene != null) {
                                Scene prevScene = finalFrameIndex > 0 ?
                                    timeline.getSceneAtTime((double) (finalFrameIndex - 1) / config.getFps()) : null;

                                BufferedImage frameImage = renderFrameToImageThreadSafe(
                                    currentScene, prevScene, currentTime, finalFrameIndex);

                                frameBuffer.put(finalFrameIndex, frameImage);
                                framesRendered.incrementAndGet();
                            }
                        } catch (Exception e) {
                            logger.error("Error rendering frame {}", finalFrameIndex, e);
                        }
                    });
                    batchFutures.add(future);
                }

                for (Future<?> f : batchFutures) {
                    f.get();
                }

                while (framesWritten.get() < batchEnd) {
                    Thread.sleep(10);
                }

                batchFutures.clear();
            }

            writerThread.join();

            long elapsedMs = System.currentTimeMillis() - startTime.get();
            double elapsedSec = elapsedMs / 1000.0;
            double speedMultiplier = totalDuration / elapsedSec;
            logger.info("Rendering completed in {:.1f} seconds ({:.1f}x realtime speed)",
                elapsedSec, speedMultiplier);

            recorder.stop();
            recorder.release();

            logger.info("Multi-threaded video rendering complete: {}", outputPath);

            converter.close();

        } catch (Exception e) {
            logger.error("Error rendering video directly", e);
            throw new RuntimeException("Direct video rendering failed", e);
        } finally {
            if (renderPool != null) {
                renderPool.shutdown();
                try {
                    if (!renderPool.awaitTermination(60, TimeUnit.SECONDS)) {
                        renderPool.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    renderPool.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
            if (tempRecorder != null) {
                try {
                    tempRecorder.close();
                } catch (Exception e) {
                    logger.warn("Error closing recorder", e);
                }
            }
        }
    }

    private BufferedImage renderFrameToImageThreadSafe(Scene scene, Scene prevScene,
                                                        double currentTime, @SuppressWarnings("unused") int frameIndex) {
        synchronized (canvasLock) {
            AtomicReference<BufferedImage> imageRef = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    clearCanvas(gc, canvas);

                    double sceneProgress = scene.getStartTime() > 0 ?
                        (currentTime - scene.getStartTime()) / scene.getDuration() : 0;

                    boolean isTransitioning = prevScene != null &&
                        !prevScene.equals(scene) &&
                        sceneProgress < 0.2;

                    if (isTransitioning) {
                        double transitionProgress = sceneProgress / 0.2;
                        renderTransitionEffect(gc, canvas, prevScene, scene, transitionProgress);
                    } else {
                        renderSceneWithAnimation(gc, canvas, scene, sceneProgress);
                    }

                    WritableImage snapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                    canvas.snapshot(null, snapshot);
                    imageRef.set(SwingFXUtils.fromFXImage(snapshot, null));
                } finally {
                    latch.countDown();
                }
            });

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }

            return imageRef.get();
        }
    }

    private void renderTransitionEffect(GraphicsContext gc, Canvas canvas, Scene prevScene, Scene newScene, double progress) {
        if (progress < 0.5) {
            renderSceneWithAnimation(gc, canvas, prevScene, 1.0);
        } else {
            renderSceneWithAnimation(gc, canvas, newScene, 0.0);
        }
    }

    private void renderSceneWithAnimation(GraphicsContext gc, Canvas canvas, Scene scene, @SuppressWarnings("unused") double progress) {
        switch (scene.getType()) {
            case INTRO -> renderIntro(gc, canvas, scene);
            case OUTRO -> renderOutro(gc, canvas, scene);
            case TITLE_SLIDE -> renderTitleSlide(gc, canvas, scene);
            case NARRATION -> renderNarration(gc, canvas, scene);
            case CODE_DISPLAY -> renderCodeDisplay(gc, canvas, scene);
            case VISUALIZATION -> renderVisualization(gc, canvas, scene);
            case ANIMATION -> renderAnimation(gc, canvas, scene);
            case TRANSITION -> renderTransition(gc, canvas);
        }
    }

    private void clearCanvas(GraphicsContext gc, Canvas canvas) {
        gc.setFill(Color.web(config.getBackgroundColor()));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void renderIntro(GraphicsContext gc, Canvas canvas, Scene scene) {
        String title = (String) scene.getVisualData("title");
        if (title == null) title = "Educational Video";

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 120));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(title, canvas.getWidth() / 2, canvas.getHeight() / 2);
    }

    private void renderOutro(GraphicsContext gc, Canvas canvas, @SuppressWarnings("unused") Scene scene) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 100));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Thank You!", canvas.getWidth() / 2, canvas.getHeight() / 2);
        gc.setFont(Font.font("Arial", 60));
        gc.fillText("Subscribe for More!", canvas.getWidth() / 2, canvas.getHeight() / 2 + 100);
    }

    private void renderTitleSlide(GraphicsContext gc, Canvas canvas, Scene scene) {
        String title = (String) scene.getVisualData("title");
        if (title != null) {
            gc.setFill(Color.web(config.getTextColor()));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 80));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(title, canvas.getWidth() / 2, canvas.getHeight() / 2);
        }
    }

    private void renderNarration(GraphicsContext gc, Canvas canvas, Scene scene) {
        String title = (String) scene.getVisualData("title");
        if (title != null) {
            gc.setFill(Color.web(config.getTextColor()));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 100));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(title, canvas.getWidth() / 2, 300);
        }

        String text = scene.getNarrationText();
        if (text != null) {
            gc.setFill(Color.web(config.getTextColor()));
            gc.setFont(Font.font("Arial", 45));
            gc.setTextAlign(TextAlignment.CENTER);
            wrapText(gc, text, canvas.getWidth() / 2, canvas.getHeight() / 2 + 100, canvas.getWidth() - 400);
        }
    }

    private void renderCodeDisplay(GraphicsContext gc, Canvas canvas, Scene scene) {
        String code = (String) scene.getVisualData("code");
        if (code != null) {
            gc.setFill(Color.WHITE);
            gc.fillRect(100, 100, canvas.getWidth() - 200, canvas.getHeight() - 200);

            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Courier New", 35));
            gc.setTextAlign(TextAlignment.LEFT);

            String[] lines = code.split("\n");
            double y = 180;
            for (String line : lines) {
                gc.fillText(line, 150, y);
                y += 50;
            }
        }
    }

    private void renderVisualization(GraphicsContext gc, Canvas canvas, Scene scene) {
        int[] array = (int[]) scene.getVisualData("array");
        Integer highlight1 = (Integer) scene.getVisualData("highlight1");
        Integer highlight2 = (Integer) scene.getVisualData("highlight2");

        if (array != null) {
            gc.setFill(Color.web(config.getTextColor()));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 90));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("Bubble Sort", canvas.getWidth() / 2, 180);

            double barWidth = (canvas.getWidth() - 300) / array.length;
            double maxBarHeight = canvas.getHeight() - 600;
            double x = 150;
            double y = 300;

            int maxValue = findMax(array);

            for (int i = 0; i < array.length; i++) {
                double barHeight = (array[i] / (double) maxValue) * maxBarHeight;
                double barX = x + i * barWidth;
                double barY = y + maxBarHeight - barHeight;

                if ((highlight1 != null && i == highlight1) || (highlight2 != null && i == highlight2)) {
                    gc.setFill(Color.web("#FFD700"));
                } else {
                    gc.setFill(Color.web("#2196F3"));
                }

                gc.fillRect(barX, barY, barWidth - 15, barHeight);

                gc.setFill(Color.web(config.getTextColor()));
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 50));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText(String.valueOf(array[i]), barX + barWidth / 2, y + maxBarHeight + 70);
            }

            String description = scene.getNarrationText();
            if (description != null) {
                gc.setFill(Color.web(config.getTextColor()));
                gc.setFont(Font.font("Arial", 55));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText(description, canvas.getWidth() / 2, canvas.getHeight() - 80);
            }
        } else {
            renderTitleSlide(gc, canvas, scene);
        }
    }

    private void renderAnimation(GraphicsContext gc, Canvas canvas, Scene scene) {
        renderTitleSlide(gc, canvas, scene);
    }

    private void renderTransition(GraphicsContext gc, Canvas canvas) {
        clearCanvas(gc, canvas);
    }

    private void wrapText(GraphicsContext gc, String text, double x, double y, @SuppressWarnings("unused") double maxWidth) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        double lineY = y;
        int wordsInLine = 0;
        int maxWordsPerLine = 12;

        for (String word : words) {
            if (wordsInLine >= maxWordsPerLine) {
                gc.fillText(line.toString().trim(), x, lineY);
                line = new StringBuilder();
                lineY += 65;
                wordsInLine = 0;
            }
            line.append(word).append(" ");
            wordsInLine++;
        }
        if (line.length() > 0) {
            gc.fillText(line.toString().trim(), x, lineY);
        }
    }

    private int findMax(int[] array) {
        int max = array[0];
        for (int value : array) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }
}
