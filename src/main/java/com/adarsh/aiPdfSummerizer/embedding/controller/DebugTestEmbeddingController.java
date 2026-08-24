package com.adarsh.aiPdfSummerizer.embedding.controller;

import com.adarsh.aiPdfSummerizer.common.dto.ApiResponse;
import com.adarsh.aiPdfSummerizer.embedding.EmbeddingClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/debug/test-embedding")
@RequiredArgsConstructor
public class DebugTestEmbeddingController {
    
    private final EmbeddingClient embeddingClient;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> testEmbedding() {
        try {
            float[] vector = embeddingClient.embed("This is a test document to check if the Gemini embedding API is working.");
            
            return ResponseEntity.ok(ApiResponse.success(Map.of(
                "status", "SUCCESS",
                "vectorLength", vector.length,
                "firstValue", vector.length > 0 ? vector[0] : 0
            )));
            
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(
                "EMBEDDING FAILED: " + e.getMessage() + (e.getCause() != null ? " | Cause: " + e.getCause().getMessage() : "")
            ));
        }
    }
}
