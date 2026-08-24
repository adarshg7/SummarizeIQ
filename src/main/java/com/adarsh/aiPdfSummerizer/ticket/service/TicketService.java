package com.adarsh.aiPdfSummerizer.ticket.service;

import com.adarsh.aiPdfSummerizer.common.dto.PageResponse;
import com.adarsh.aiPdfSummerizer.common.exception.BadRequestException;
import com.adarsh.aiPdfSummerizer.common.exception.ResourceNotFoundException;
import com.adarsh.aiPdfSummerizer.embedding.service.EmbeddingIndexService;
import com.adarsh.aiPdfSummerizer.notification.NotificationService;
import com.adarsh.aiPdfSummerizer.ticket.dto.TicketCreateRequest;
import com.adarsh.aiPdfSummerizer.ticket.dto.TicketResponse;
import com.adarsh.aiPdfSummerizer.ticket.entity.Ticket;
import com.adarsh.aiPdfSummerizer.ticket.entity.TicketStatus;
import com.adarsh.aiPdfSummerizer.ticket.repository.TicketRepository;
import com.adarsh.aiPdfSummerizer.user.entity.User;
import com.adarsh.aiPdfSummerizer.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final NotificationService notificationService;
    private final EmbeddingIndexService embeddingIndexService;

    @Transactional
    public TicketResponse create(TicketCreateRequest request, String customerEmail) {
           User customer = userRepository.findByEmail(customerEmail)
                   .orElseThrow(() -> new ResourceNotFoundException("User","email",customerEmail));

        Ticket ticket = new Ticket();
        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setStatus(TicketStatus.OPEN);

        if(request.getChatSessionId() != null){
            ticket.setChatSession(entityManager.getReference(
                    com.adarsh.aiPdfSummerizer.chat.entity.ChatSession.class,request.getChatSessionId()
            ));
        }

        ticket.setCustomer(customer); // ← was missing, caused customer_id = null
        Ticket saved = ticketRepository.save(ticket);
        notificationService.sendTicketCreatedEmail(
                customer.getEmail(),customer.getFullName(), saved.getId(), saved.getSubject()
        );
        return TicketResponse.from(saved);
    }

    @Transactional
    public TicketResponse close(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket","id",ticketId));

        if(ticket.getStatus() == TicketStatus.CLOSED){
            throw new BadRequestException("Ticket is already closed");
        }

        // Extract customer fields NOW (while session is open from findById)
        // Using plain Strings avoids LazyInitializationException when passed to notification service
        String customerEmail = ticket.getCustomer().getEmail();
        String customerName = ticket.getCustomer().getFullName();

        ticket.setStatus(TicketStatus.CLOSED);
        Ticket saved = ticketRepository.save(ticket);
        notificationService.sendTicketClosedEmail(customerEmail, customerName, saved.getId());
        embeddingIndexService.indexTicket(saved.getId()); // pass ID, reloaded safely inside async thread
        return TicketResponse.from(saved);
    }

    @Transactional
    public TicketResponse assignAgent(Long ticketId, String agentEmail) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BadRequestException("Cannot assign an agent to a closed ticket");
        }
        
        User agent = userRepository.findByEmail(agentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", agentEmail));

        ticket.setAgent(agent);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        Ticket saved = ticketRepository.save(ticket);
        return TicketResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> getByStatus(TicketStatus status, Pageable pageable) {
        return PageResponse.from(
                ticketRepository.findByStatus(status, pageable).map(TicketResponse::from)
        );
    }
}
