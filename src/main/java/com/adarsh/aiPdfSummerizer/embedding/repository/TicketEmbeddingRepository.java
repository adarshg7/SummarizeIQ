package com.adarsh.aiPdfSummerizer.embedding.repository;

import com.adarsh.aiPdfSummerizer.embedding.entity.TicketEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketEmbeddingRepository extends JpaRepository<TicketEmbedding, Long> {
    // Fetch ALL embeddings for a customer's tickets → compared in-memory.
    // Fine at small scale; a real vector DB would push this comparison
    // down into the database/index instead.
    List<TicketEmbedding> findByTicket_Customer_Id(Long customerId);
}