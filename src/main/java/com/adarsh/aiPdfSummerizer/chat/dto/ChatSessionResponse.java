package com.adarsh.aiPdfSummerizer.chat.dto;

import com.adarsh.aiPdfSummerizer.chat.entity.ChatSession;
import com.adarsh.aiPdfSummerizer.chat.entity.ChatSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatSessionResponse {

    private Long id;
    private String customerName;
    private String agentName; // null until assigned
    private ChatSessionStatus status;

    public static ChatSessionResponse from(ChatSession session) {
        return new ChatSessionResponse(
                session.getId(),
                session.getCustomer().getFullName(),
                session.getAgent() != null ? session.getAgent().getFullName() : null,
                session.getStatus()
        );
    }
}