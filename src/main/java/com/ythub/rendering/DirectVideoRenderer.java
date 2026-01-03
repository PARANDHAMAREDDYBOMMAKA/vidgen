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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class DirectVideoRenderer {

    private static final Logger logger = LoggerFactory.getLogger(DirectVideoRenderer.class);

    private final VideoConfig config;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private static boolean javaFXInitialized = false;

    public DirectVideoRenderer(VideoConfig config) {
        this.config = config;

        if (!javaFXInitialized) {
            initJavaFX();
            javaFXInitialized = true;
        }

        AtomicReference<Canvas> canvasRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            canvasRef.set(new Canvas(config.getWidth(), config.getHeight()));
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        this.canvas = canvasRef.get();
        this.gc = canvas.getGraphicsContext2D();
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
        FFmpegFrameRecorder recorder = null;
        Java2DFrameConverter converter = new Java2DFrameConverter();

        try {
            recorder = new FFmpegFrameRecorder(outputPath, config.getWidth(), config.getHeight());
            recorder.setFrameRate(config.getFps());
            recorder.setVideoBitrate(config.getBitrate() * 1000);

            if ("hevc".equalsIgnoreCase(config.getCodec())) {
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_HEVC);
            } else {
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            }

            recorder.setFormat(config.getFormat());
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            recorder.start();

            double totalDuration = timeline.getTotalDuration();
            int totalFrames = (int) (totalDuration * config.getFps());

            logger.info("Rendering {} frames directly to video ({} seconds)", totalFrames, totalDuration);

            for (int frameIndex = 0; frameIndex < totalFrames; frameIndex++) {
                double currentTime = (double) frameIndex / config.getFps();
                Scene currentScene = timeline.getSceneAtTime(currentTime);

                if (currentScene != null) {
                    BufferedImage frameImage = renderFrameToImage(currentScene, currentTime);
                    Frame frame = converter.convert(frameImage);
                    recorder.record(frame);

                    if (frameIndex % (config.getFps() * 10) == 0) {
                        int secondsRendered = frameIndex / config.getFps();
                        int totalSeconds = (int) totalDuration;
                        logger.info("Progress: {}/{} seconds ({}/{}%)",
                            secondsRendered, totalSeconds,
                            (frameIndex * 100 / totalFrames), 100);
                    }
                }
            }

            recorder.stop();
            recorder.release();

            logger.info("Direct video rendering complete: {}", outputPath);

        } catch (Exception e) {
            logger.error("Error rendering video directly", e);
            throw new RuntimeException("Direct video rendering failed", e);
        } finally {
            if (recorder != null) {
                try {
                    recorder.close();
                } catch (Exception e) {
                    logger.warn("Error closing recorder", e);
                }
            }
            converter.close();
        }
    }

    private BufferedImage renderFrameToImage(Scene scene, @SuppressWarnings("unused") double currentTime) {
        CountDownLatch renderLatch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                clearCanvas();

                switch (scene.getType()) {
                    case INTRO -> renderIntro(scene);
                    case OUTRO -> renderOutro(scene);
                    case TITLE_SLIDE -> renderTitleSlide(scene);
                    case NARRATION -> renderNarration(scene);
                    case CODE_DISPLAY -> renderCodeDisplay(scene);
                    case VISUALIZATION -> renderVisualization(scene);
                    case ANIMATION -> renderAnimation(scene);
                    case TRANSITION -> renderTransition();
                }
            } finally {
                renderLatch.countDown();
            }
        });

        try {
            renderLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        AtomicReference<BufferedImage> imageRef = new AtomicReference<>();
        CountDownLatch captureLatch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                WritableImage snapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                canvas.snapshot(null, snapshot);
                imageRef.set(SwingFXUtils.fromFXImage(snapshot, null));
            } finally {
                captureLatch.countDown();
            }
        });

        try {
            captureLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return imageRef.get();
    }

    private void clearCanvas() {
        gc.setFill(Color.web(config.getBackgroundColor()));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void renderIntro(Scene scene) {
        String title = (String) scene.getVisualData("title");
        if (title == null) title = "Educational Video";

        gc.setFill(Color.web(config.getPrimaryColor()));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 120));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(title, canvas.getWidth() / 2, canvas.getHeight() / 2);
    }

    private void renderOutro(@SuppressWarnings("unused") Scene scene) {
        gc.setFill(Color.web(config.getSecondaryColor()));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 100));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Thank You!", canvas.getWidth() / 2, canvas.getHeight() / 2);
        gc.setFont(Font.font("Arial", 60));
        gc.fillText("Subscribe for More!", canvas.getWidth() / 2, canvas.getHeight() / 2 + 100);
    }

    private void renderTitleSlide(Scene scene) {
        String title = (String) scene.getVisualData("title");
        if (title != null) {
            gc.setFill(Color.web(config.getTextColor()));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 80));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(title, canvas.getWidth() / 2, canvas.getHeight() / 2);
        }
    }

    private void renderNarration(Scene scene) {
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
            wrapText(text, canvas.getWidth() / 2, canvas.getHeight() / 2 + 100, canvas.getWidth() - 400);
        }
    }

    private void renderCodeDisplay(Scene scene) {
        String code = (String) scene.getVisualData("code");
        if (code != null) {
            gc.setFill(Color.web("#282C34"));
            gc.fillRect(100, 100, canvas.getWidth() - 200, canvas.getHeight() - 200);

            gc.setFill(Color.web("#ABB2BF"));
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

    private void renderVisualization(Scene scene) {
        int[] array = (int[]) scene.getVisualData("array");
        Integer highlight1 = (Integer) scene.getVisualData("highlight1");
        Integer highlight2 = (Integer) scene.getVisualData("highlight2");

        if (array != null) {
            gc.setFill(Color.BLACK);
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
                    gc.setFill(Color.RED);
                } else {
                    gc.setFill(Color.web("#2196F3"));
                }

                gc.fillRect(barX, barY, barWidth - 15, barHeight);

                gc.setFill(Color.BLACK);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 50));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText(String.valueOf(array[i]), barX + barWidth / 2, y + maxBarHeight + 70);
            }

            String description = scene.getNarrationText();
            if (description != null) {
                gc.setFill(Color.BLACK);
                gc.setFont(Font.font("Arial", 55));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText(description, canvas.getWidth() / 2, canvas.getHeight() - 80);
            }
        } else {
            renderTitleSlide(scene);
        }
    }

    private void renderAnimation(Scene scene) {
        renderTitleSlide(scene);
    }

    private void renderTransition() {
        clearCanvas();
    }

    private void wrapText(String text, double x, double y, @SuppressWarnings("unused") double maxWidth) {
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
