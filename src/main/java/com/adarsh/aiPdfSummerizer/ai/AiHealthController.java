package com.adarsh.aiPdfSummerizer.ai;

import com.adarsh.aiPdfSummerizer.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Quick health-check endpoint to verify Groq AI is reachable and responding.
 * Call GET /api/ai/test to confirm the model is working before testing summarization.
 * Remove or secure this endpoint before going to production.
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiHealthController {

    private final AiClient aiClient;

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> testAi(
            @RequestParam(defaultValue = "Say hello in one sentence.") String prompt) {
        try {
            String response = aiClient.generateResponse(prompt);
            return ResponseEntity.ok(ApiResponse.success("AI is working ✅", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("AI call FAILED ❌: " + e.getMessage()));
        }
    }
}
