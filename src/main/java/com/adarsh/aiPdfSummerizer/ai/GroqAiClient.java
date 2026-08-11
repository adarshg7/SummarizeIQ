package com.adarsh.aiPdfSummerizer.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;


@Service
public class GroqAiClient implements AiClient{
    @Value("${ai.provider.base-url}")
    private String baseUrl;

    @Value("${ai.provider.api-key}")
    private String apiKey;

    @Value("${ai.provider.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateResponse(String prompt){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String,Object> requestBody = Map.of(
                "model",model,
                "messages", List.of(
                        Map.of("role","user","content",prompt)
                )
        );

        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(requestBody,headers);

        try{
            ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.POST,entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                throw new RuntimeException("AI provider returned empty choices");
            }
            return choices.get(0).path("message").path("content").asText();

        } catch (Exception e){
            throw new RuntimeException("AI provider call failed: " + e.getMessage(),e);
        }
    }
}
