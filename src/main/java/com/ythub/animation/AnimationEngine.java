package com.ythub.animation;

import javafx.scene.canvas.GraphicsContext;

public class AnimationEngine {

    private GraphicsContext gc;
    private int width;
    private int height;

    public AnimationEngine(GraphicsContext gc, int width, int height) {
        this.gc = gc;
        this.width = width;
        this.height = height;
    }

    public void drawArray(int[] array, int highlightIndex1, int highlightIndex2, double x, double y, double barWidth, double maxBarHeight) {
        int maxValue = findMax(array);

        for (int i = 0; i < array.length; i++) {
            double barHeight = (array[i] / (double) maxValue) * maxBarHeight;
            double barX = x + i * barWidth;
            double barY = y + maxBarHeight - barHeight;

            if (i == highlightIndex1 || i == highlightIndex2) {
                gc.setFill(javafx.scene.paint.Color.RED);
            } else {
                gc.setFill(javafx.scene.paint.Color.web("#2196F3"));
            }

            gc.fillRect(barX, barY, barWidth - 5, barHeight);

            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial", 30));
            gc.fillText(String.valueOf(array[i]), barX + barWidth / 4, y + maxBarHeight + 40);
        }
    }

    public void drawArray(int[] array, double x, double y, double barWidth, double maxBarHeight) {
        drawArray(array, -1, -1, x, y, barWidth, maxBarHeight);
    }

    public void drawText(String text, double x, double y, double fontSize) {
        gc.setFill(javafx.scene.paint.Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font("Arial", fontSize));
        gc.fillText(text, x, y);
    }

    public void drawTitle(String title) {
        gc.setFill(javafx.scene.paint.Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 80));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.fillText(title, width / 2.0, 150);
    }

    public void clear() {
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillRect(0, 0, width, height);
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

    public GraphicsContext getGraphicsContext() {
        return gc;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
