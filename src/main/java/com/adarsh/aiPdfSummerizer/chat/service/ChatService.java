package com.adarsh.aiPdfSummerizer.chat.service;

import com.adarsh.aiPdfSummerizer.chat.dto.ChatMessageResponse;
import com.adarsh.aiPdfSummerizer.chat.entity.ChatSession;
import com.adarsh.aiPdfSummerizer.chat.entity.Message;
import com.adarsh.aiPdfSummerizer.chat.repository.ChatSessionRepository;
import com.adarsh.aiPdfSummerizer.chat.repository.MessageRepository;
import com.adarsh.aiPdfSummerizer.common.exception.ResourceNotFoundException;
import com.adarsh.aiPdfSummerizer.queue.AiJob;
import com.adarsh.aiPdfSummerizer.cache.AiResponseCacheService;
import com.adarsh.aiPdfSummerizer.queue.AiJobQueue;
import com.adarsh.aiPdfSummerizer.ticket.dto.TicketCreateRequest;
import com.adarsh.aiPdfSummerizer.ticket.service.TicketService;
import com.adarsh.aiPdfSummerizer.user.entity.User;
import com.adarsh.aiPdfSummerizer.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final AiJobQueue aiJobQueue;
    private final SimpMessagingTemplate messagingTemplate;
    private final TicketService ticketService;
    private final AiResponseCacheService aiResponseCacheService;

    @org.springframework.transaction.annotation.Transactional
    public ChatMessageResponse handleIncomingMessage(Long chatSessionId, String senderEmail, String content) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", senderEmail));

        ChatSession session = chatSessionRepository.findById(chatSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatSession", "id", chatSessionId));

        Message message = new Message();
        message.setChatSession(entityManager.getReference(ChatSession.class, chatSessionId));
        message.setSender(sender);
        message.setContent(content); // <-- was missing before
        message.setFromAi(false);

        Message saved = messageRepository.save(message);
        ChatMessageResponse response = ChatMessageResponse.from(saved);

        messagingTemplate.convertAndSend("/topic/chat/" + chatSessionId, response);

        if (session.getAgent() == null) {
            queueAiReply(session, content);
        }

        return response;
    }

    private void queueAiReply(ChatSession session, String customerMessage) {
        // Extract required fields NOW while we are still inside the JPA transaction
        Long sessionId = session.getId();
        String customerEmail = session.getCustomer().getEmail();

        // Check cache first — skip the AI call entirely on a hit.
        Optional<String> cached = aiResponseCacheService.get(customerMessage);
        if (cached.isPresent()) {
            saveAndBroadcastAiReply(sessionId, customerEmail, cached.get());
            return;
        }

        String prompt = "You are a customer support assistant. Answer concisely. Question: " + customerMessage;

        aiJobQueue.submit(new AiJob(
                prompt,
                aiResult -> {
                    aiResponseCacheService.put(customerMessage, aiResult); // cache it for next time
                    saveAndBroadcastAiReply(sessionId, customerEmail, aiResult);
                },
                error -> escalateToTicket(sessionId, customerEmail, customerMessage, "AI service error")
        ));
    }

    private void saveAndBroadcastAiReply(Long sessionId, String customerEmail, String aiResult) {
        Message aiMessage = new Message();
        aiMessage.setChatSession(entityManager.getReference(ChatSession.class, sessionId));
        aiMessage.setSender(null);
        aiMessage.setContent(aiResult);
        aiMessage.setFromAi(true);
        Message saved = messageRepository.save(aiMessage);

        messagingTemplate.convertAndSend("/topic/chat/" + sessionId, ChatMessageResponse.from(saved));

        if (aiCouldNotAnswer(aiResult)) {
            escalateToTicket(sessionId, customerEmail, "See chat session #" + sessionId + " for context", "AI could not resolve query");
        }
    }

    private void escalateToTicket(Long sessionId, String customerEmail, String description, String reasonSubject) {
        TicketCreateRequest ticketRequest = new TicketCreateRequest();
        ticketRequest.setSubject(reasonSubject);
        ticketRequest.setDescription(description);
        ticketRequest.setChatSessionId(sessionId);

        ticketService.create(ticketRequest, customerEmail);
    }

    private boolean aiCouldNotAnswer(String aiResponse) {
        if (aiResponse == null || aiResponse.isBlank()) return true;
        String lower = aiResponse.toLowerCase();
        return lower.contains("i don't know") || lower.contains("i'm not sure")
                || lower.contains("cannot help") || lower.contains("i am unable");
    }
}