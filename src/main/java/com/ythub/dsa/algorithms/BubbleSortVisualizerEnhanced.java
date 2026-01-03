package com.ythub.dsa.algorithms;

import com.ythub.animation.AnimationEngine;
import com.ythub.core.Scene;
import com.ythub.core.Timeline;
import com.ythub.core.VideoConfig;

import java.util.ArrayList;
import java.util.List;

public class BubbleSortVisualizerEnhanced {

    private final VideoConfig config;
    private final AnimationEngine animationEngine;

    public BubbleSortVisualizerEnhanced(VideoConfig config, AnimationEngine animationEngine) {
        this.config = config;
        this.animationEngine = animationEngine;
    }

    public Timeline createComprehensiveBubbleSortVideo() {
        Timeline timeline = new Timeline("Bubble Sort Algorithm - Complete Guide");

        addIntroSection(timeline);
        addConceptExplanation(timeline);
        addAlgorithmWalkthrough(timeline);
        addComplexityAnalysis(timeline);
        addExample1SmallArray(timeline);
        addExample2LargerArray(timeline);
        addExample3ReverseSorted(timeline);
        addExample4AlreadySorted(timeline);
        addCodeImplementation(timeline);
        addOptimizedVersion(timeline);
        addComparisons(timeline);
        addPracticeProblems(timeline);
        addRealWorldApplications(timeline);
        addSummaryAndConclusion(timeline);
        addOutro(timeline);

        return timeline;
    }

    private void addIntroSection(Timeline timeline) {
        Scene intro = new Scene.Builder(Scene.SceneType.INTRO, 5)
                .withVisualData("title", "Bubble Sort Algorithm")
                .build();
        timeline.addScene(intro);

        Scene welcome = new Scene.Builder(Scene.SceneType.NARRATION, 15)
                .withNarration("Welcome to this comprehensive guide on Bubble Sort algorithm. In this video, we will cover everything you need to know about Bubble Sort, from basic concepts to advanced optimizations.")
                .withVisualData("title", "What You'll Learn")
                .build();
        timeline.addScene(welcome);

        Scene agenda = new Scene.Builder(Scene.SceneType.NARRATION, 20)
                .withNarration("We'll start with understanding what Bubble Sort is and how it works. Then we'll analyze its time and space complexity. After that, we'll visualize the algorithm with multiple examples, look at code implementation, explore optimizations, and finally discuss real-world applications.")
                .withVisualData("title", "Course Outline")
                .build();
        timeline.addScene(agenda);
    }

    private void addConceptExplanation(Timeline timeline) {
        Scene concept1 = new Scene.Builder(Scene.SceneType.NARRATION, 25)
                .withNarration("Bubble Sort is one of the simplest sorting algorithms to understand and implement. It gets its name from the way smaller or larger elements bubble up to their correct positions, similar to how bubbles rise in water.")
                .withVisualData("title", "What is Bubble Sort?")
                .build();
        timeline.addScene(concept1);

        Scene concept2 = new Scene.Builder(Scene.SceneType.NARRATION, 30)
                .withNarration("The algorithm works by repeatedly stepping through the list to be sorted. It compares each pair of adjacent items and swaps them if they are in the wrong order. This pass through the list is repeated until no more swaps are needed, which means the list is sorted.")
                .withVisualData("title", "How Does It Work?")
                .build();
        timeline.addScene(concept2);

        Scene concept3 = new Scene.Builder(Scene.SceneType.NARRATION, 25)
                .withNarration("In each pass through the array, the largest unsorted element bubbles up to its correct position at the end. After the first pass, the largest element is in its final position. After the second pass, the second largest element is in place, and so on.")
                .withVisualData("title", "The Bubbling Process")
                .build();
        timeline.addScene(concept3);
    }

    private void addAlgorithmWalkthrough(Timeline timeline) {
        Scene walk1 = new Scene.Builder(Scene.SceneType.NARRATION, 30)
                .withNarration("Let's break down the algorithm step by step. Step one: Start at the beginning of the array. Step two: Compare the first two elements. Step three: If the first element is greater than the second, swap them. Step four: Move to the next pair and repeat.")
                .withVisualData("title", "Algorithm Steps")
                .build();
        timeline.addScene(walk1);

        Scene walk2 = new Scene.Builder(Scene.SceneType.NARRATION, 25)
                .withNarration("Continue this process until you reach the end of the array. This completes one pass. Then, repeat the entire process for the remaining unsorted portion of the array. Continue until no swaps are made in a complete pass.")
                .withVisualData("title", "Multiple Passes")
                .build();
        timeline.addScene(walk2);
    }

    private void addComplexityAnalysis(Timeline timeline) {
        Scene complexity1 = new Scene.Builder(Scene.SceneType.NARRATION, 35)
                .withNarration("Now let's analyze the time complexity of Bubble Sort. In the worst case scenario, when the array is sorted in reverse order, we need to make n minus one passes through the array. In each pass, we make approximately n comparisons. This gives us a time complexity of O of n squared.")
                .withVisualData("title", "Worst Case: O(n²)")
                .build();
        timeline.addScene(complexity1);

        Scene complexity2 = new Scene.Builder(Scene.SceneType.NARRATION, 30)
                .withNarration("In the best case, when the array is already sorted, Bubble Sort with optimization can detect this in a single pass through the array, giving us O of n time complexity. However, the standard implementation still takes O of n squared time.")
                .withVisualData("title", "Best Case: O(n)")
                .build();
        timeline.addScene(complexity2);

        Scene complexity3 = new Scene.Builder(Scene.SceneType.NARRATION, 25)
                .withNarration("The space complexity of Bubble Sort is O of 1, meaning it only requires a constant amount of additional memory for the temporary variable used during swapping. This makes it an in-place sorting algorithm.")
                .withVisualData("title", "Space Complexity: O(1)")
                .build();
        timeline.addScene(complexity3);
    }

    private void addExample1SmallArray(Timeline timeline) {
        Scene exampleIntro = new Scene.Builder(Scene.SceneType.NARRATION, 15)
                .withNarration("Let's now visualize Bubble Sort with our first example. We'll start with a small array to clearly see how each element moves to its correct position.")
                .withVisualData("title", "Example 1: Small Array")
                .build();
        timeline.addScene(exampleIntro);

        int[] array1 = {5, 2, 8, 1, 9};
        addSortingVisualization(timeline, array1, 2.0);

        Scene exampleSummary = new Scene.Builder(Scene.SceneType.NARRATION, 15)
                .withNarration("As you can see, the array is now completely sorted. Notice how the larger elements bubbled up to the right side of the array with each pass.")
                .withVisualData("title", "Example 1 Complete")
                .build();
        timeline.addScene(exampleSummary);
    }

    private void addExample2LargerArray(Timeline timeline) {
        Scene exampleIntro = new Scene.Builder(Scene.SceneType.NARRATION, 15)
                .withNarration("Now let's look at a slightly larger array to see how Bubble Sort performs with more elements. Pay attention to how many comparisons and swaps are needed.")
                .withVisualData("title", "Example 2: Larger Array")
                .build();
        timeline.addScene(exampleIntro);

        int[] array2 = {64, 34, 25, 12, 22, 11, 90, 88};
        addSortingVisualization(timeline, array2, 1.5);

        Scene exampleSummary = new Scene.Builder(Scene.SceneType.NARRATION, 20)
                .withNarration("With eight elements, you can see the algorithm required multiple passes. Each pass reduced the unsorted portion of the array by one element. This demonstrates why the time complexity is O of n squared.")
                .withVisualData("title", "Example 2 Analysis")
                .build();
        timeline.addScene(exampleSummary);
    }

    private void addExample3ReverseSorted(Timeline timeline) {
        Scene exampleIntro = new Scene.Builder(Scene.SceneType.NARRATION, 20)
                .withNarration("Let's examine the worst case scenario: an array sorted in reverse order. This requires the maximum number of swaps as every pair of adjacent elements needs to be swapped.")
                .withVisualData("title", "Example 3: Worst Case")
                .build();
        timeline.addScene(exampleIntro);

        int[] array3 = {9, 8, 7, 6, 5, 4};
        addSortingVisualization(timeline, array3, 1.5);

        Scene exampleSummary = new Scene.Builder(Scene.SceneType.NARRATION, 15)
                .withNarration("As expected, this required the maximum number of comparisons and swaps. This is why Bubble Sort is inefficient for large datasets, especially when they are in reverse order.")
                .withVisualData("title", "Worst Case Performance")
                .build();
        timeline.addScene(exampleSummary);
    }

    private void addExample4AlreadySorted(Timeline timeline) {
        Scene exampleIntro = new Scene.Builder(Scene.SceneType.NARRATION, 20)
                .withNarration("Now let's see what happens when the array is already sorted. With an optimized version of Bubble Sort, we can detect this early and stop.")
                .withVisualData("title", "Example 4: Best Case")
                .build();
        timeline.addScene(exampleIntro);

        int[] array4 = {1, 2, 3, 4, 5, 6};
        addSortingVisualization(timeline, array4, 1.5);

        Scene exampleSummary = new Scene.Builder(Scene.SceneType.NARRATION, 15)
                .withNarration("Notice that even though the array was already sorted, the basic Bubble Sort still went through multiple passes. This is where optimization comes in, which we'll discuss next.")
                .withVisualData("title", "Need for Optimization")
                .build();
        timeline.addScene(exampleSummary);
    }

    private void addCodeImplementation(Timeline timeline) {
        Scene codeIntro = new Scene.Builder(Scene.SceneType.NARRATION, 15)
                .withNarration("Let's look at the code implementation of Bubble Sort. We'll examine both the basic version and an optimized version.")
                .withVisualData("title", "Code Implementation")
                .build();
        timeline.addScene(codeIntro);

        String basicCode = "void bubbleSort(int arr[]) {\n" +
                "  int n = arr.length;\n" +
                "  for (int i = 0; i < n-1; i++) {\n" +
                "    for (int j = 0; j < n-i-1; j++) {\n" +
                "      if (arr[j] > arr[j+1]) {\n" +
                "        int temp = arr[j];\n" +
                "        arr[j] = arr[j+1];\n" +
                "        arr[j+1] = temp;\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        Scene codeDisplay = new Scene.Builder(Scene.SceneType.CODE_DISPLAY, 35)
                .withNarration("Here's the basic implementation. The outer loop runs n minus one times, and the inner loop compares adjacent elements and swaps them if needed. Notice the inner loop goes up to n minus i minus one to avoid checking already sorted elements.")
                .withVisualData("code", basicCode)
                .build();
        timeline.addScene(codeDisplay);
    }

    private void addOptimizedVersion(Timeline timeline) {
        Scene optIntro = new Scene.Builder(Scene.SceneType.NARRATION, 20)
                .withNarration("We can optimize Bubble Sort by adding a flag to detect if any swaps were made in a pass. If no swaps occur, the array is already sorted and we can stop early.")
                .withVisualData("title", "Optimized Bubble Sort")
                .build();
        timeline.addScene(optIntro);

        String optimizedCode = "void bubbleSortOptimized(int arr[]) {\n" +
                "  int n = arr.length;\n" +
                "  boolean swapped;\n" +
                "  for (int i = 0; i < n-1; i++) {\n" +
                "    swapped = false;\n" +
                "    for (int j = 0; j < n-i-1; j++) {\n" +
                "      if (arr[j] > arr[j+1]) {\n" +
                "        swap(arr, j, j+1);\n" +
                "        swapped = true;\n" +
                "      }\n" +
                "    }\n" +
                "    if (!swapped) break;\n" +
                "  }\n" +
                "}";

        Scene optCode = new Scene.Builder(Scene.SceneType.CODE_DISPLAY, 30)
                .withNarration("The optimized version uses a boolean flag. If the flag remains false after a complete pass, meaning no swaps were made, we break out of the loop early. This improves best case performance to O of n.")
                .withVisualData("code", optimizedCode)
                .build();
        timeline.addScene(optCode);
    }

    private void addComparisons(Timeline timeline) {
        Scene comparison = new Scene.Builder(Scene.SceneType.NARRATION, 35)
                .withNarration("How does Bubble Sort compare to other sorting algorithms? While it's easy to understand and implement, it's generally slower than algorithms like Quick Sort, Merge Sort, or Heap Sort for large datasets. However, for small arrays or nearly sorted data, it can be quite efficient.")
                .withVisualData("title", "Comparison with Other Algorithms")
                .build();
        timeline.addScene(comparison);
    }

    private void addPracticeProblems(Timeline timeline) {
        Scene practice1 = new Scene.Builder(Scene.SceneType.NARRATION, 25)
                .withNarration("Let's discuss some practice problems. First, try modifying Bubble Sort to sort in descending order instead of ascending. Second, implement a version that counts the number of swaps made during sorting.")
                .withVisualData("title", "Practice Problem 1 & 2")
                .build();
        timeline.addScene(practice1);

        Scene practice2 = new Scene.Builder(Scene.SceneType.NARRATION, 25)
                .withNarration("Third, write a function to detect if an array is nearly sorted using Bubble Sort. Fourth, implement a recursive version of Bubble Sort. These exercises will help solidify your understanding of the algorithm.")
                .withVisualData("title", "Practice Problem 3 & 4")
                .build();
        timeline.addScene(practice2);
    }

    private void addRealWorldApplications(Timeline timeline) {
        Scene applications = new Scene.Builder(Scene.SceneType.NARRATION, 30)
                .withNarration("While Bubble Sort is rarely used in production code for large-scale applications, it has its place. It's useful for educational purposes, small datasets, and situations where simplicity is valued over performance. It's also used in embedded systems with memory constraints due to its O of 1 space complexity.")
                .withVisualData("title", "Real-World Applications")
                .build();
        timeline.addScene(applications);
    }

    private void addSummaryAndConclusion(Timeline timeline) {
        Scene summary = new Scene.Builder(Scene.SceneType.NARRATION, 30)
                .withNarration("To summarize, Bubble Sort is a simple comparison-based sorting algorithm with O of n squared time complexity and O of 1 space complexity. While not efficient for large datasets, it's excellent for learning sorting concepts and works well for small or nearly sorted arrays.")
                .withVisualData("title", "Summary")
                .build();
        timeline.addScene(summary);

        Scene keyPoints = new Scene.Builder(Scene.SceneType.NARRATION, 25)
                .withNarration("Key takeaways: Bubble Sort is easy to implement. It's stable and in-place. Best case is O of n with optimization. Worst and average case is O of n squared. Not suitable for large datasets. Great for learning and teaching.")
                .withVisualData("title", "Key Takeaways")
                .build();
        timeline.addScene(keyPoints);
    }

    private void addOutro(Timeline timeline) {
        Scene outro = new Scene.Builder(Scene.SceneType.OUTRO, 10)
                .build();
        timeline.addScene(outro);
    }

    private void addSortingVisualization(Timeline timeline, int[] array, double speedMultiplier) {
        List<SortStep> steps = generateBubbleSortSteps(array.clone());
        for (SortStep step : steps) {
            Scene visualScene = new Scene.Builder(Scene.SceneType.VISUALIZATION, 0.5 * speedMultiplier)
                    .withNarration(step.description)
                    .withVisualData("array", step.array)
                    .withVisualData("highlight1", step.index1)
                    .withVisualData("highlight2", step.index2)
                    .build();
            timeline.addScene(visualScene);
        }
    }

    private List<SortStep> generateBubbleSortSteps(int[] array) {
        List<SortStep> steps = new ArrayList<>();
        int n = array.length;

        steps.add(new SortStep(array.clone(), -1, -1, "Starting array"));

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                steps.add(new SortStep(array.clone(), j, j + 1,
                    String.format("Comparing %d and %d", array[j], array[j + 1])));

                if (array[j] > array[j + 1]) {
                    int temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;

                    steps.add(new SortStep(array.clone(), j, j + 1,
                        String.format("Swapped %d and %d", array[j + 1], array[j])));
                }
            }
        }

        steps.add(new SortStep(array.clone(), -1, -1, "Sorting complete!"));

        return steps;
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
