package com.adarsh.aiPdfSummerizer.chat.repository;

import com.adarsh.aiPdfSummerizer.chat.entity.ChatSession;
import com.adarsh.aiPdfSummerizer.chat.entity.ChatSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession,Long> {
    List<ChatSession> findByStatusAndUpdatedAtBefore(ChatSessionStatus status, LocalDateTime cutoff);
}
