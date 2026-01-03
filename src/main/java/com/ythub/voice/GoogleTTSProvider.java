package com.ythub.voice;

import com.ythub.core.VideoConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GoogleTTSProvider implements TTSProvider {

    private static final Logger logger = LoggerFactory.getLogger(GoogleTTSProvider.class);
    private static final String TTS_URL = "https://translate.google.com/translate_tts";
    private final OkHttpClient client;
    private final VideoConfig config;

    public GoogleTTSProvider(VideoConfig config) {
        this.client = new OkHttpClient();
        this.config = config;
    }

    @Override
    public byte[] generateAudio(String text) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            logger.warn("Empty text provided, returning silence");
            return new byte[0];
        }

        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);

        String url = TTS_URL + "?ie=UTF-8&q=" + encodedText
                + "&tl=en&client=tw-ob"
                + "&ttsspeed=" + config.getTtsSpeed();

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .build();

        logger.info("Generating free Google TTS audio for text: {} (length: {} chars)",
                text.substring(0, Math.min(50, text.length())), text.length());

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error details";
                throw new IOException("Google TTS error: " + response.code() + " - " + errorBody);
            }

            byte[] audioData = response.body().bytes();
            logger.info("Google TTS audio generated successfully, size: {} bytes", audioData.length);
            return audioData;
        }
    }

    @Override
    public String getProviderName() {
        return "Google TTS (Free)";
    }

    @Override
    public boolean isConfigured() {
        return true;
    }
}
