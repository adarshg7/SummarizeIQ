package com.adarsh.aiPdfSummerizer.ticket.repository;

import com.adarsh.aiPdfSummerizer.ticket.entity.Ticket;
import com.adarsh.aiPdfSummerizer.ticket.entity.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);
    Page<Ticket> findByCustomerId(Long customerId, Pageable pageable);
    long countByStatus(TicketStatus status);   // <-- added
}