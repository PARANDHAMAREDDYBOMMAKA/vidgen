package com.ythub.core;

import java.util.ArrayList;
import java.util.List;

public class Timeline {

    private List<Scene> scenes;
    private double totalDuration;
    private String videoTitle;

    public Timeline(String videoTitle) {
        this.videoTitle = videoTitle;
        this.scenes = new ArrayList<>();
        this.totalDuration = 0;
    }

    public void addScene(Scene scene) {
        scene.setStartTime(totalDuration);
        scenes.add(scene);
        totalDuration += scene.getDuration();
    }

    public void addScenes(List<Scene> scenesToAdd) {
        for (Scene scene : scenesToAdd) {
            addScene(scene);
        }
    }

    public void insertScene(int index, Scene scene) {
        if (index < 0 || index > scenes.size()) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }

        scenes.add(index, scene);
        recalculateTiming();
    }

    public boolean removeScene(String sceneId) {
        boolean removed = scenes.removeIf(s -> s.getId().equals(sceneId));
        if (removed) {
            recalculateTiming();
        }
        return removed;
    }

    public Scene removeScene(int index) {
        if (index < 0 || index >= scenes.size()) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }

        Scene removed = scenes.remove(index);
        recalculateTiming();
        return removed;
    }

    public Scene getSceneAtTime(double time) {
        for (Scene scene : scenes) {
            if (time >= scene.getStartTime() && time < scene.getEndTime()) {
                return scene;
            }
        }
        return null;
    }

    public Scene getSceneById(String sceneId) {
        return scenes.stream()
                .filter(s -> s.getId().equals(sceneId))
                .findFirst()
                .orElse(null);
    }

    private void recalculateTiming() {
        double currentTime = 0;
        for (Scene scene : scenes) {
            scene.setStartTime(currentTime);
            currentTime += scene.getDuration();
        }
        totalDuration = currentTime;
    }

    public void clear() {
        scenes.clear();
        totalDuration = 0;
    }

    public int getSceneCount() {
        return scenes.size();
    }

    public boolean isEmpty() {
        return scenes.isEmpty();
    }

    public List<Scene> getScenes() {
        return new ArrayList<>(scenes);
    }

    public double getTotalDuration() {
        return totalDuration;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getFormattedDuration() {
        int minutes = (int) (totalDuration / 60);
        int seconds = (int) (totalDuration % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public String toString() {
        return String.format("Timeline{title='%s', scenes=%d, duration=%s}",
                videoTitle, scenes.size(), getFormattedDuration());
    }

    public void printSummary() {
        System.out.println("=== Timeline Summary ===");
        System.out.println("Title: " + videoTitle);
        System.out.println("Total Scenes: " + scenes.size());
        System.out.println("Total Duration: " + getFormattedDuration());
        System.out.println("\nScenes:");
        for (int i = 0; i < scenes.size(); i++) {
            Scene scene = scenes.get(i);
            System.out.printf("%d. %s [%.2fs - %.2fs] (%.2fs)\n",
                    i + 1, scene.getType(), scene.getStartTime(),
                    scene.getEndTime(), scene.getDuration());
        }
        System.out.println("========================");
    }
}
