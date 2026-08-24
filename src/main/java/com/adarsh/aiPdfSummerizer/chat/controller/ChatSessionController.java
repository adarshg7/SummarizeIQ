package com.adarsh.aiPdfSummerizer.chat.controller;

import com.adarsh.aiPdfSummerizer.chat.dto.ChatSessionResponse;
import com.adarsh.aiPdfSummerizer.chat.service.ChatSessionService;
import com.adarsh.aiPdfSummerizer.common.dto.ApiResponse;
import com.adarsh.aiPdfSummerizer.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Ordinary @RestController — this is plain HTTP, NOT STOMP.
 * Starting/closing a session happens once via REST; only the
 * back-and-forth MESSAGES within a session go over WebSocket
 * (that's ChatController, a @Controller with @MessageMapping).
 * Don't confuse the two — different transport, different classes.
 */
@RestController
@RequestMapping("/api/chat/sessions")
@RequiredArgsConstructor
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatSessionResponse>> start() {
        String email = SecurityUtils.getCurrentUserEmail();
        ChatSessionResponse response = chatSessionService.startSession(email);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Chat session started", response));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<ApiResponse<ChatSessionResponse>> close(@PathVariable Long id) {
        ChatSessionResponse response = chatSessionService.closeSession(id);
        return ResponseEntity.ok(ApiResponse.success("Chat session closed", response));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<ChatSessionResponse>> assignAgent(@PathVariable Long id) {
        String agentEmail = SecurityUtils.getCurrentUserEmail();
        ChatSessionResponse response = chatSessionService.assignAgent(id, agentEmail);
        return ResponseEntity.ok(ApiResponse.success("Agent assigned to chat session", response));
    }
}