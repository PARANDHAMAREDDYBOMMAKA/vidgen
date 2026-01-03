package com.ythub.dsa.algorithms;

import com.ythub.animation.AnimationEngine;
import com.ythub.core.Scene;
import com.ythub.core.Timeline;
import com.ythub.core.VideoConfig;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

public class BubbleSortVisualizer {

    private final VideoConfig config;
    private final AnimationEngine animationEngine;

    public BubbleSortVisualizer(VideoConfig config, AnimationEngine animationEngine) {
        this.config = config;
        this.animationEngine = animationEngine;
    }

    public Timeline createBubbleSortVideo(int[] array) {
        Timeline timeline = new Timeline("Bubble Sort Algorithm");

        Scene intro = new Scene.Builder(Scene.SceneType.INTRO, 3)
                .withVisualData("title", "Bubble Sort")
                .build();
        timeline.addScene(intro);

        Scene explanation = new Scene.Builder(Scene.SceneType.NARRATION, 8)
                .withNarration("Bubble Sort is a simple sorting algorithm that repeatedly steps through the list, compares adjacent elements, and swaps them if they are in the wrong order.")
                .withVisualData("title", "What is Bubble Sort?")
                .build();
        timeline.addScene(explanation);

        Scene complexity = new Scene.Builder(Scene.SceneType.NARRATION, 6)
                .withNarration("The time complexity of Bubble Sort is O of n squared in the worst and average case, and O of n in the best case when the array is already sorted.")
                .withVisualData("title", "Time Complexity: O(n²)")
                .build();
        timeline.addScene(complexity);

        List<SortStep> steps = generateBubbleSortSteps(array.clone());
        for (SortStep step : steps) {
            Scene visualScene = new Scene.Builder(Scene.SceneType.VISUALIZATION, 0.5)
                    .withNarration(step.description)
                    .withVisualData("array", step.array)
                    .withVisualData("highlight1", step.index1)
                    .withVisualData("highlight2", step.index2)
                    .build();
            timeline.addScene(visualScene);
        }

        Scene outro = new Scene.Builder(Scene.SceneType.OUTRO, 3)
                .build();
        timeline.addScene(outro);

        return timeline;
    }

    private List<SortStep> generateBubbleSortSteps(int[] array) {
        List<SortStep> steps = new ArrayList<>();
        int n = array.length;

        steps.add(new SortStep(array.clone(), -1, -1, "Initial array"));

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                steps.add(new SortStep(array.clone(), j, j + 1,
                    String.format("Comparing %d and %d", array[j], array[j + 1])));

                if (array[j] > array[j + 1]) {
                    int temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;

                    steps.add(new SortStep(array.clone(), j, j + 1,
                        String.format("Swapping %d and %d", array[j + 1], array[j])));
                }
            }
        }

        steps.add(new SortStep(array.clone(), -1, -1, "Array is now sorted!"));

        return steps;
    }

    public void renderBubbleSortFrame(GraphicsContext gc, int[] array, int highlight1, int highlight2, String description) {
        animationEngine.clear();
        animationEngine.drawTitle("Bubble Sort");

        double barWidth = (double) config.getWidth() / array.length - 20;
        double maxBarHeight = config.getHeight() - 400;
        double x = 100;
        double y = 250;

        animationEngine.drawArray(array, highlight1, highlight2, x, y, barWidth, maxBarHeight);

        animationEngine.drawText(description, config.getWidth() / 2.0 - 300, config.getHeight() - 100, 40);
    }

    private static class SortStep {
        int[] array;
        int index1;
        int index2;
        String description;

        SortStep(int[] array, int index1, int index2, String description) {
            this.array = array;
            this.index1 = index1;
            this.index2 = index2;
            this.description = description;
        }
    }
}
