package com.adarsh.aiPdfSummerizer.chat.service;

import com.adarsh.aiPdfSummerizer.chat.dto.ChatSessionResponse;
import com.adarsh.aiPdfSummerizer.chat.entity.ChatSession;
import com.adarsh.aiPdfSummerizer.chat.entity.ChatSessionStatus;
import com.adarsh.aiPdfSummerizer.chat.repository.ChatSessionRepository;
import com.adarsh.aiPdfSummerizer.common.exception.ResourceNotFoundException;
import com.adarsh.aiPdfSummerizer.user.entity.User;
import com.adarsh.aiPdfSummerizer.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final UserRepository userRepository;

    public ChatSessionResponse startSession(String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", customerEmail));

        ChatSession session = new ChatSession();
        session.setCustomer(customer);
        session.setStatus(ChatSessionStatus.OPEN);
        // agent stays null -> this is exactly what makes ChatService
        // route the first message to the AI assistant instead of a human.

        ChatSession saved = chatSessionRepository.save(session);
        return ChatSessionResponse.from(saved);
    }

    public ChatSessionResponse closeSession(Long sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatSession", "id", sessionId));
        session.setStatus(ChatSessionStatus.CLOSED);
        return ChatSessionResponse.from(chatSessionRepository.save(session));
    }

    public ChatSessionResponse assignAgent(Long sessionId, String agentEmail) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatSession", "id", sessionId));
        User agent = userRepository.findByEmail(agentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", agentEmail));

        session.setAgent(agent);
        return ChatSessionResponse.from(chatSessionRepository.save(session));
    }
}