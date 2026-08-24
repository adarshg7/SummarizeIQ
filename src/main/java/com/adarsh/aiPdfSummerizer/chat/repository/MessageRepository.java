package com.adarsh.aiPdfSummerizer.chat.repository;

import com.adarsh.aiPdfSummerizer.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message,Long> {
    List<Message> findByChatSessionIdOrderByCreatedAtAsc(Long chatSessionId);

}
