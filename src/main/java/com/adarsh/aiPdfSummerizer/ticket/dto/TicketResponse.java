package com.adarsh.aiPdfSummerizer.ticket.dto;

import com.adarsh.aiPdfSummerizer.ticket.entity.Ticket;
import com.adarsh.aiPdfSummerizer.ticket.entity.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TicketResponse {
    private Long id;
    private String subject;
    private String description;
    private TicketStatus status;
    private String customerName;
    private String agentName;
    private LocalDateTime createdAt;

    public static TicketResponse from(Ticket ticket){
        return new TicketResponse(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getCustomer().getFullName(),
                ticket.getAgent() != null ? ticket.getAgent().getFullName() : null,
                ticket.getCreatedAt()
        );
    }
}
