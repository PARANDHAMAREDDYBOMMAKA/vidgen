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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class FrameRenderer {

    private static final Logger logger = LoggerFactory.getLogger(FrameRenderer.class);

    private final VideoConfig config;
    private final Canvas canvas;
    private final GraphicsContext gc;

    private static boolean javaFXInitialized = false;

    public FrameRenderer(VideoConfig config) {
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

    public List<String> renderTimeline(Timeline timeline) {
        List<String> framePaths = new ArrayList<>();

        double totalDuration = timeline.getTotalDuration();
        int totalFrames = (int) (totalDuration * config.getFps());

        logger.info("Rendering {} frames for {} seconds", totalFrames, totalDuration);

        for (int frameIndex = 0; frameIndex < totalFrames; frameIndex++) {
            double currentTime = (double) frameIndex / config.getFps();
            Scene currentScene = timeline.getSceneAtTime(currentTime);

            if (currentScene != null) {
                String framePath = renderFrame(currentScene, currentTime, frameIndex);
                framePaths.add(framePath);

                if (frameIndex % config.getFps() == 0) {
                    logger.info("Rendered {}/{} frames", frameIndex, totalFrames);
                }
            }
        }

        return framePaths;
    }

    private String renderFrame(Scene scene, double currentTime, int frameIndex) {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                clearCanvas();

                switch (scene.getType()) {
                    case INTRO:
                        renderIntro(scene, currentTime);
                        break;
                    case OUTRO:
                        renderOutro(scene, currentTime);
                        break;
                    case TITLE_SLIDE:
                        renderTitleSlide(scene, currentTime);
                        break;
                    case NARRATION:
                        renderNarration(scene, currentTime);
                        break;
                    case CODE_DISPLAY:
                        renderCodeDisplay(scene, currentTime);
                        break;
                    case VISUALIZATION:
                        renderVisualization(scene, currentTime);
                        break;
                    case ANIMATION:
                        renderAnimation(scene, currentTime);
                        break;
                    case TRANSITION:
                        renderTransition(scene, currentTime);
                        break;
                }
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String framePath = config.getTempDir() + "/frame_" + String.format("%06d", frameIndex) + ".png";
        saveFrame(framePath);

        return framePath;
    }

    private void clearCanvas() {
        gc.setFill(Color.web(config.getBackgroundColor()));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void renderIntro(Scene scene, double currentTime) {
        String title = (String) scene.getVisualData("title");
        if (title == null) title = "Educational Video";

        gc.setFill(Color.web(config.getPrimaryColor()));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 120));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(title, canvas.getWidth() / 2, canvas.getHeight() / 2);
    }

    private void renderOutro(Scene scene, double currentTime) {
        gc.setFill(Color.web(config.getSecondaryColor()));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 100));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Thank You!", canvas.getWidth() / 2, canvas.getHeight() / 2);
    }

    private void renderTitleSlide(Scene scene, double currentTime) {
        String title = (String) scene.getVisualData("title");
        if (title != null) {
            gc.setFill(Color.web(config.getTextColor()));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 80));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(title, canvas.getWidth() / 2, 200);
        }
    }

    private void renderNarration(Scene scene, double currentTime) {
        String text = scene.getNarrationText();
        if (text != null) {
            gc.setFill(Color.web(config.getTextColor()));
            gc.setFont(Font.font("Arial", 50));
            gc.setTextAlign(TextAlignment.CENTER);
            wrapText(text, canvas.getWidth() / 2, canvas.getHeight() / 2, canvas.getWidth() - 200);
        }
    }

    private void renderCodeDisplay(Scene scene, double currentTime) {
        String code = (String) scene.getVisualData("code");
        if (code != null) {
            gc.setFill(Color.web("#282C34"));
            gc.fillRect(100, 100, canvas.getWidth() - 200, canvas.getHeight() - 200);

            gc.setFill(Color.web("#ABB2BF"));
            gc.setFont(Font.font("Courier New", 40));
            gc.setTextAlign(TextAlignment.LEFT);

            String[] lines = code.split("\n");
            double y = 150;
            for (String line : lines) {
                gc.fillText(line, 120, y);
                y += 50;
            }
        }
    }

    private void renderVisualization(Scene scene, double currentTime) {
        int[] array = (int[]) scene.getVisualData("array");
        Integer highlight1 = (Integer) scene.getVisualData("highlight1");
        Integer highlight2 = (Integer) scene.getVisualData("highlight2");

        if (array != null) {
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 80));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("Bubble Sort", canvas.getWidth() / 2, 150);

            double barWidth = (canvas.getWidth() - 200) / array.length;
            double maxBarHeight = canvas.getHeight() - 500;
            double x = 100;
            double y = 250;

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

                gc.fillRect(barX, barY, barWidth - 10, barHeight);

                gc.setFill(Color.BLACK);
                gc.setFont(Font.font("Arial", 40));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText(String.valueOf(array[i]), barX + barWidth / 2, y + maxBarHeight + 60);
            }

            String description = scene.getNarrationText();
            if (description != null) {
                gc.setFill(Color.BLACK);
                gc.setFont(Font.font("Arial", 50));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText(description, canvas.getWidth() / 2, canvas.getHeight() - 100);
            }
        } else {
            renderTitleSlide(scene, currentTime);
        }
    }

    private void renderAnimation(Scene scene, double currentTime) {
        renderTitleSlide(scene, currentTime);
    }

    private void renderTransition(Scene scene, double currentTime) {
        clearCanvas();
    }

    private void wrapText(String text, double x, double y, double maxWidth) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        double lineY = y;

        for (String word : words) {
            String testLine = line + word + " ";
            if (gc.getFont().getSize() * testLine.length() / 2 > maxWidth) {
                gc.fillText(line.toString(), x, lineY);
                line = new StringBuilder(word + " ");
                lineY += 60;
            } else {
                line.append(word).append(" ");
            }
        }
        gc.fillText(line.toString(), x, lineY);
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

    private void saveFrame(String path) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<WritableImage> imageRef = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                WritableImage image = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                canvas.snapshot(null, image);
                imageRef.set(image);
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await();
            File file = new File(path);
            file.getParentFile().mkdirs();
            ImageIO.write(SwingFXUtils.fromFXImage(imageRef.get(), null), "png", file);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while saving frame: {}", path, e);
        } catch (IOException e) {
            logger.error("Failed to save frame: {}", path, e);
        }
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public GraphicsContext getGraphicsContext() {
        return gc;
    }
}
