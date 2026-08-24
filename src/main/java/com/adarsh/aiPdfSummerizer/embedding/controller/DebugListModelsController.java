package com.adarsh.aiPdfSummerizer.embedding.controller;

import com.adarsh.aiPdfSummerizer.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/debug/list-models")
@RequiredArgsConstructor
public class DebugListModelsController {
    
    @Value("${embedding.provider.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> listModels() {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;
            String response = restTemplate.getForObject(url, String.class);
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(
                "FAILED TO LIST MODELS: " + e.getMessage()
            ));
        }
    }
}
