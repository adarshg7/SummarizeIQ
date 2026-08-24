package com.adarsh.aiPdfSummerizer.embedding.controller;

import com.adarsh.aiPdfSummerizer.common.dto.ApiResponse;
import com.adarsh.aiPdfSummerizer.embedding.service.RagQueryService;
import com.adarsh.aiPdfSummerizer.security.SecurityUtils;
import com.adarsh.aiPdfSummerizer.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagQueryService ragQueryService;
    private final UserService userService;

    /**
     * Ask a question answered from your uploaded PDFs.
     * GET /api/rag/ask?question=What are the key findings?
     */
    @GetMapping("/ask")
    public ResponseEntity<ApiResponse<String>> askFromPdfs(@RequestParam String question) {
        String email = SecurityUtils.getCurrentUserEmail();
        Long userId = userService.getByEmail(email).getId();
        String answer = ragQueryService.answerFromPdfs(userId, question);
        return ResponseEntity.ok(ApiResponse.success(answer));
    }

    /**
     * Ask a question answered from your past support tickets.
     * GET /api/rag/summarize-tickets?question=What login issues did I report?
     */
    @GetMapping("/summarize-tickets")
    public ResponseEntity<ApiResponse<String>> summarizeTickets(@RequestParam String question) {
        String email = SecurityUtils.getCurrentUserEmail();
        Long userId = userService.getByEmail(email).getId();
        String answer = ragQueryService.summarizePreviousTickets(userId, question);
        return ResponseEntity.ok(ApiResponse.success(answer));
    }
}