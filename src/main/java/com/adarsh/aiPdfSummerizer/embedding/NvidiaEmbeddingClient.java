package com.adarsh.aiPdfSummerizer.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Embedding client for NVIDIA NIM (nemotron-3-embed-1b).
 * NVIDIA NIM uses an OpenAI-compatible /v1/embeddings REST API,
 * so we send a standard Bearer token + {model, input} payload.
 */
@Service
public class NvidiaEmbeddingClient implements EmbeddingClient {

    @Value("${embedding.provider.base-url}")
    private String baseUrl;

    @Value("${embedding.provider.api-key}")
    private String apiKey;

    @Value("${embedding.provider.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public float[] embed(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // NVIDIA NIM uses OpenAI-compatible request shape
        Map<String, Object> body = Map.of(
                "model", model,
                "input", text
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(baseUrl, HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode valuesNode = root.path("data").get(0).path("embedding");

            float[] vector = new float[valuesNode.size()];
            for (int i = 0; i < valuesNode.size(); i++) {
                vector[i] = (float) valuesNode.get(i).asDouble();
            }
            return vector;

        } catch (Exception e) {
            throw new RuntimeException("NVIDIA NIM embedding call failed: " + e.getMessage(), e);
        }
    }
}
