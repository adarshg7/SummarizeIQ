package com.adarsh.aiPdfSummerizer.scheduler;

import com.adarsh.aiPdfSummerizer.chat.entity.ChatSessionStatus;
import com.adarsh.aiPdfSummerizer.chat.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Runs every hour, closes any chat session that's been OPEN with no
 * activity for 24+ hours. Prevents abandoned sessions from sitting
 * "open" forever and skewing any "active chats" dashboard count.
 */
@Component
@RequiredArgsConstructor
public class ChatCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(ChatCleanupJob.class);

    private final ChatSessionRepository chatSessionRepository;

    // cron: second, minute, hour, day, month, weekday -> "0 0 * * * *" = top of every hour
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void closeInactiveSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);

        List<com.adarsh.aiPdfSummerizer.chat.entity.ChatSession> staleSessions =
                chatSessionRepository.findByStatusAndUpdatedAtBefore(ChatSessionStatus.OPEN, cutoff);

        if (staleSessions.isEmpty()) {
            log.info("Chat cleanup: no inactive sessions found");
            return;
        }

        staleSessions.forEach(session -> session.setStatus(ChatSessionStatus.CLOSED));
        chatSessionRepository.saveAll(staleSessions);

        log.info("Chat cleanup: closed {} inactive session(s)", staleSessions.size());
    }
}