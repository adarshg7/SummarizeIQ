package com.adarsh.aiPdfSummerizer.ticket.controller;

import com.adarsh.aiPdfSummerizer.common.dto.ApiResponse;
import com.adarsh.aiPdfSummerizer.common.dto.PageResponse;
import com.adarsh.aiPdfSummerizer.security.SecurityUtils;
import com.adarsh.aiPdfSummerizer.ticket.dto.TicketCreateRequest;
import com.adarsh.aiPdfSummerizer.ticket.dto.TicketResponse;
import com.adarsh.aiPdfSummerizer.ticket.entity.TicketStatus;
import com.adarsh.aiPdfSummerizer.ticket.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> create(@Valid @RequestBody TicketCreateRequest request) {
        String email = SecurityUtils.getCurrentUserEmail();
        TicketResponse response = ticketService.create(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Ticket created", response));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<ApiResponse<TicketResponse>> close(@PathVariable Long id) {
        TicketResponse response = ticketService.close(id);
        return ResponseEntity.ok(ApiResponse.success("Ticket closed", response));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<TicketResponse>> assign(@PathVariable Long id, @RequestParam String agentEmail) {
        TicketResponse response = ticketService.assignAgent(id, agentEmail);
        return ResponseEntity.ok(ApiResponse.success("Agent assigned", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TicketResponse>>> getByStatus(
            @RequestParam TicketStatus status, Pageable pageable) {
        PageResponse<TicketResponse> response = ticketService.getByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
