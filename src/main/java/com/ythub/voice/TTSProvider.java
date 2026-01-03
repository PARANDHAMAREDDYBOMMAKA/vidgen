package com.ythub.voice;

import java.io.IOException;

public interface TTSProvider {

    byte[] generateAudio(String text) throws IOException;

    String getProviderName();

    boolean isConfigured();
}
