package com.adarsh.aiPdfSummerizer.chat.controller;

import com.adarsh.aiPdfSummerizer.chat.dto.ChatMessageRequest;
import com.adarsh.aiPdfSummerizer.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageRequest request, Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("User is not authenticated");
        }
        chatService.handleIncomingMessage(
                request.getChatSessionId(),
                principal.getName(),
                request.getContent()
        );
    }
}