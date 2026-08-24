package com.adarsh.aiPdfSummerizer.embedding;

/**
 * Same boundary pattern as AiClient — every other class depends on
 * THIS interface, never on the OpenAI-specific implementation directly.
 */
public interface EmbeddingClient {
    float[] embed(String text);
}