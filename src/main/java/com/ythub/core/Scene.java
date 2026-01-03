package com.ythub.core;

import java.util.HashMap;
import java.util.Map;

public class Scene {

    public enum SceneType {
        INTRO,
        OUTRO,
        TITLE_SLIDE,
        NARRATION,
        CODE_DISPLAY,
        VISUALIZATION,
        ANIMATION,
        TRANSITION
    }

    private String id;
    private SceneType type;
    private double duration;
    private String narrationText;
    private Map<String, Object> visualData;
    private Map<String, Object> animationConfig;
    private double startTime;
    private double endTime;

    public Scene(String id, SceneType type, double duration) {
        this.id = id;
        this.type = type;
        this.duration = duration;
        this.visualData = new HashMap<>();
        this.animationConfig = new HashMap<>();
    }

    public Scene(SceneType type, double duration) {
        this(generateId(), type, duration);
    }

    private static String generateId() {
        return "scene_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    public static class Builder {
        private Scene scene;

        public Builder(SceneType type, double duration) {
            scene = new Scene(type, duration);
        }

        public Builder withId(String id) {
            scene.id = id;
            return this;
        }

        public Builder withNarration(String text) {
            scene.narrationText = text;
            return this;
        }

        public Builder withVisualData(String key, Object value) {
            scene.visualData.put(key, value);
            return this;
        }

        public Builder withAnimationConfig(String key, Object value) {
            scene.animationConfig.put(key, value);
            return this;
        }

        public Builder withStartTime(double startTime) {
            scene.startTime = startTime;
            return this;
        }

        public Scene build() {
            scene.endTime = scene.startTime + scene.duration;
            return scene;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SceneType getType() {
        return type;
    }

    public void setType(SceneType type) {
        this.type = type;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
        this.endTime = this.startTime + duration;
    }

    public String getNarrationText() {
        return narrationText;
    }

    public void setNarrationText(String narrationText) {
        this.narrationText = narrationText;
    }

    public Map<String, Object> getVisualData() {
        return visualData;
    }

    public void setVisualData(Map<String, Object> visualData) {
        this.visualData = visualData;
    }

    public void addVisualData(String key, Object value) {
        this.visualData.put(key, value);
    }

    public Object getVisualData(String key) {
        return visualData.get(key);
    }

    public Map<String, Object> getAnimationConfig() {
        return animationConfig;
    }

    public void setAnimationConfig(Map<String, Object> animationConfig) {
        this.animationConfig = animationConfig;
    }

    public void addAnimationConfig(String key, Object value) {
        this.animationConfig.put(key, value);
    }

    public Object getAnimationConfig(String key) {
        return animationConfig.get(key);
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
        this.endTime = startTime + duration;
    }

    public double getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return String.format("Scene{id='%s', type=%s, duration=%.2fs, start=%.2fs, end=%.2fs}",
                id, type, duration, startTime, endTime);
    }
}
