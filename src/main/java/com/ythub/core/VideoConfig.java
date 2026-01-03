package com.ythub.core;

public class VideoConfig {

    public enum Resolution {
        HD_1080P_30(1920, 1080, 30, "1080p30"),
        HD_1080P_60(1920, 1080, 60, "1080p60"),
        UHD_4K(3840, 2160, 60, "4K60"),
        UHD_8K(7680, 4320, 60, "8K60");

        public final int width;
        public final int height;
        public final int fps;
        public final String name;

        Resolution(int width, int height, int fps, String name) {
            this.width = width;
            this.height = height;
            this.fps = fps;
            this.name = name;
        }
    }

    private Resolution resolution = Resolution.UHD_4K;
    private int fps = 60;
    private String codec = "hevc";
    private int bitrate = 45000;
    private String format = "mp4";

    private int audioSampleRate = 48000;
    private int audioBitrate = 320;
    private int audioChannels = 2;

    private String primaryColor = "#FFFFFF";
    private String secondaryColor = "#FFFFFF";
    private String backgroundColor = "#FFFFFF";
    private String textColor = "#000000";
    private String codeTheme = "monokai";

    private String ttsProvider = "google";
    private String ttsApiKey = null;
    private String ttsVoice = "21m00Tcm4TlvDq8ikWAM";
    private String ttsGender = "male";
    private float ttsSpeed = 1.0f;
    private float ttsPitch = 0.0f;

    private String outputDir = "output";
    private String tempDir = "temp";
    private String assetsDir = "src/main/resources/assets";
    private String scriptsDir = "src/main/resources/scripts";

    public VideoConfig() {
    }

    public VideoConfig(Resolution resolution) {
        this.resolution = resolution;
        adjustBitrateForResolution();
    }

    private void adjustBitrateForResolution() {
        this.fps = resolution.fps;
        switch (resolution) {
            case HD_1080P_30:
                this.bitrate = 8000;
                break;
            case HD_1080P_60:
                this.bitrate = 12000;
                break;
            case UHD_4K:
                this.bitrate = 45000;
                break;
            case UHD_8K:
                this.bitrate = 85000;
                break;
        }
    }

    public Resolution getResolution() {
        return resolution;
    }

    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
        adjustBitrateForResolution();
    }

    public int getWidth() {
        return resolution.width;
    }

    public int getHeight() {
        return resolution.height;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getAudioSampleRate() {
        return audioSampleRate;
    }

    public void setAudioSampleRate(int audioSampleRate) {
        this.audioSampleRate = audioSampleRate;
    }

    public int getAudioBitrate() {
        return audioBitrate;
    }

    public void setAudioBitrate(int audioBitrate) {
        this.audioBitrate = audioBitrate;
    }

    public int getAudioChannels() {
        return audioChannels;
    }

    public void setAudioChannels(int audioChannels) {
        this.audioChannels = audioChannels;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getCodeTheme() {
        return codeTheme;
    }

    public void setCodeTheme(String codeTheme) {
        this.codeTheme = codeTheme;
    }

    public String getTtsProvider() {
        return ttsProvider;
    }

    public void setTtsProvider(String ttsProvider) {
        this.ttsProvider = ttsProvider;
    }

    public String getTtsApiKey() {
        return ttsApiKey;
    }

    public void setTtsApiKey(String ttsApiKey) {
        this.ttsApiKey = ttsApiKey;
    }

    public String getTtsVoice() {
        return ttsVoice;
    }

    public void setTtsVoice(String ttsVoice) {
        this.ttsVoice = ttsVoice;
    }

    public float getTtsSpeed() {
        return ttsSpeed;
    }

    public void setTtsSpeed(float ttsSpeed) {
        this.ttsSpeed = ttsSpeed;
    }

    public float getTtsPitch() {
        return ttsPitch;
    }

    public void setTtsPitch(float ttsPitch) {
        this.ttsPitch = ttsPitch;
    }

    public String getTtsGender() {
        return ttsGender;
    }

    public void setTtsGender(String ttsGender) {
        this.ttsGender = ttsGender;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public String getAssetsDir() {
        return assetsDir;
    }

    public void setAssetsDir(String assetsDir) {
        this.assetsDir = assetsDir;
    }

    public String getScriptsDir() {
        return scriptsDir;
    }

    public void setScriptsDir(String scriptsDir) {
        this.scriptsDir = scriptsDir;
    }

    @Override
    public String toString() {
        return String.format("VideoConfig{resolution=%s, fps=%d, codec='%s', bitrate=%d kbps}",
                resolution.name, fps, codec, bitrate);
    }
}
