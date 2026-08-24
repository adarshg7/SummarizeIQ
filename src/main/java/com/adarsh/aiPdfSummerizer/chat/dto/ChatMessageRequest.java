package com.adarsh.aiPdfSummerizer.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequest {
    private Long chatSessionId;
    private String content;
}
