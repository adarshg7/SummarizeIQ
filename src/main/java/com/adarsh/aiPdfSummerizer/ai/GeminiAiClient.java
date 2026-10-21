package com.adarsh.aiPdfSummerizer.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * AiClient implementation backed by Google Gemini via the native REST API.
 *
 * Uses RestTemplate to POST to the Gemini generateContent endpoint with API key
 * authentication (query parameter). No additional SDK dependency required.
 *
 * Marked @Primary so Spring picks this over GroqAiClient wherever AiClient is injected.
 */
@Slf4j
@Primary
@Service
public class GeminiAiClient implements AiClient {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.8-flash}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateResponse(String prompt) {
        List<String> candidateModels = new java.util.ArrayList<>();
        if (model != null && !model.isBlank()) {
            candidateModels.add(model.trim());
        }
        for (String m : List.of("gemini-3.5-flash", "gemini-flash-lite-latest", "gemini-3.8-flash")) {
            if (!candidateModels.contains(m)) {
                candidateModels.add(m);
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        Exception lastException = null;

        for (String candidate : candidateModels) {
            String url = String.format(
                    "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                    candidate, apiKey);

            for (int attempt = 1; attempt <= 2; attempt++) {
                try {
                    ResponseEntity<String> response = restTemplate.exchange(
                            url, HttpMethod.POST, entity, String.class);

                    JsonNode root = objectMapper.readTree(response.getBody());
                    JsonNode candidates = root.path("candidates");

                    if (!candidates.isArray() || candidates.isEmpty()) {
                        throw new RuntimeException("Gemini returned empty candidates");
                    }

                    String text = candidates.get(0)
                            .path("content")
                            .path("parts")
                            .get(0)
                            .path("text")
                            .asText();

                    log.info("[GeminiAiClient] Successfully generated response ({} chars) using model={}", text.length(), candidate);
                    return text;

                } catch (org.springframework.web.client.HttpStatusCodeException e) {
                    int status = e.getStatusCode().value();
                    lastException = e;
                    log.warn("[GeminiAiClient] Model={} attempt {} failed with HTTP {}: {}", candidate, attempt, status, e.getResponseBodyAsString());

                    if (status == 503 || status == 429 || status == 404) {
                        // High demand or rate limit or discontinued model on this candidate -> fallback immediately to next candidate
                        log.info("[GeminiAiClient] Model={} is unavailable (HTTP {}). Trying fallback model...", candidate, status);
                        break; // exit retry loop and advance to next candidate model
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during wait", ie);
                    }
                } catch (Exception e) {
                    lastException = e;
                    log.warn("[GeminiAiClient] Model={} attempt {} failed: {}", candidate, attempt, e.getMessage());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during wait", ie);
                    }
                }
            }
        }

        throw new RuntimeException("All Gemini candidate models failed. Last error: " + (lastException != null ? lastException.getMessage() : "unknown"));
    }
}
