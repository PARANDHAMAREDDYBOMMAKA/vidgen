package com.ythub.voice;

public class SilentTTSProvider implements TTSProvider {

    private final int sampleRate;

    public SilentTTSProvider(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    @Override
    public byte[] generateAudio(String text) {
        int durationMs = text.length() * 50;
        int numSamples = (sampleRate * durationMs) / 1000;
        return new byte[numSamples * 2];
    }

    @Override
    public String getProviderName() {
        return "Silent (Placeholder)";
    }

    @Override
    public boolean isConfigured() {
        return true;
    }
}
