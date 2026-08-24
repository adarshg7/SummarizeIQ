package com.adarsh.aiPdfSummerizer.chat.dto;

import com.adarsh.aiPdfSummerizer.chat.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private Long chatSessionId;
    private String senderName;
    private String content;
    private boolean fromAi;
    private LocalDateTime sentAt;

    public static ChatMessageResponse from(Message message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getChatSession().getId(),
                message.isFromAi() ? "AI Assistant" : message.getSender().getFullName(), // senderName slot
                message.getContent(),
                message.isFromAi(),      // fromAi slot
                message.getCreatedAt()   // sentAt slot
        );
    }
}