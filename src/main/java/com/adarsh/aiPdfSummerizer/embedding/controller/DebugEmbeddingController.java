package com.adarsh.aiPdfSummerizer.embedding.controller;

import com.adarsh.aiPdfSummerizer.common.dto.ApiResponse;
import com.adarsh.aiPdfSummerizer.document.entity.Document;
import com.adarsh.aiPdfSummerizer.document.repository.DocumentRepository;
import com.adarsh.aiPdfSummerizer.embedding.EmbeddingClient;
import com.adarsh.aiPdfSummerizer.embedding.VectorMathUtil;
import com.adarsh.aiPdfSummerizer.embedding.entity.DocumentEmbedding;
import com.adarsh.aiPdfSummerizer.embedding.entity.TicketEmbedding;
import com.adarsh.aiPdfSummerizer.embedding.repository.DocumentEmbeddingRepository;
import com.adarsh.aiPdfSummerizer.embedding.repository.TicketEmbeddingRepository;
import com.adarsh.aiPdfSummerizer.ticket.entity.Ticket;
import com.adarsh.aiPdfSummerizer.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/debug/embeddings")
@RequiredArgsConstructor
public class DebugEmbeddingController {

    private final DocumentEmbeddingRepository documentEmbeddingRepository;
    private final DocumentRepository documentRepository;
    private final EmbeddingClient embeddingClient;
    private final TicketEmbeddingRepository ticketEmbeddingRepository;
    private final TicketRepository ticketRepository;

    /**
     * Returns count of ALL embeddings in the DB — no lazy loading.
     * GET /api/debug/embeddings
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllEmbeddings() {
        long docCount = documentEmbeddingRepository.count();
        long ticketCount = ticketEmbeddingRepository.count();
        return ResponseEntity.ok(ApiResponse.success(Map.of(
            "documentEmbeddings", docCount,
            "ticketEmbeddings", ticketCount,
            "note", ticketCount == 0 ? "No ticket embeddings yet. Use POST /api/debug/embeddings/trigger-ticket/{ticketId}" : "Ticket embeddings found!"
        )));
    }

    /**
     * Manually trigger embedding for a document — SYNCHRONOUS, result is immediate.
     * POST /api/debug/embeddings/trigger/{documentId}
     */
    @PostMapping("/trigger/{documentId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> triggerEmbedding(@PathVariable Long documentId) {
        try {
            Document document = documentRepository.findById(documentId).orElse(null);
            if (document == null) {
                return ResponseEntity.ok(ApiResponse.error("Document " + documentId + " not found in DB"));
            }

            String text = document.getExtractedText();
            if (text == null || text.isBlank()) {
                return ResponseEntity.ok(ApiResponse.error("Document " + documentId + " has no extracted text"));
            }

            // Remove existing embedding if any
            documentEmbeddingRepository.findByDocument_Id(documentId)
                    .ifPresent(documentEmbeddingRepository::delete);

            float[] vector = embeddingClient.embed(text);

            DocumentEmbedding embedding = new DocumentEmbedding();
            embedding.setDocument(document);
            embedding.setSourceText(text);
            embedding.setVectorData(VectorMathUtil.toStorageString(vector));
            documentEmbeddingRepository.save(embedding);

            return ResponseEntity.ok(ApiResponse.success(Map.of(
                "status", "SUCCESS",
                "documentId", documentId,
                "fileName", document.getFileName(),
                "textLength", text.length(),
                "vectorLength", vector.length
            )));

        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("FAILED: " + e.getMessage()));
        }
    }
    /**
     * Manually trigger embedding for a ticket — SYNCHRONOUS, result is immediate.
     * POST /api/debug/embeddings/trigger-ticket/{ticketId}
     */
    @PostMapping("/trigger-ticket/{ticketId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> triggerTicketEmbedding(@PathVariable Long ticketId) {
        try {
            Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
            if (ticket == null) {
                return ResponseEntity.ok(ApiResponse.error("Ticket " + ticketId + " not found in DB"));
            }

            String text = ticket.getSubject() + ". " + ticket.getDescription();
            if (text.isBlank()) {
                return ResponseEntity.ok(ApiResponse.error("Ticket " + ticketId + " has no subject or description"));
            }

            // Remove existing embedding if any
            ticketEmbeddingRepository.findByTicket_Customer_Id(ticket.getCustomer().getId())
                    .stream()
                    .filter(e -> e.getTicket().getId().equals(ticketId))
                    .findFirst()
                    .ifPresent(ticketEmbeddingRepository::delete);

            float[] vector = embeddingClient.embed(text);

            TicketEmbedding embedding = new TicketEmbedding();
            embedding.setTicket(ticket);
            embedding.setSourceText(text);
            embedding.setVectorData(VectorMathUtil.toStorageString(vector));
            ticketEmbeddingRepository.save(embedding);

            return ResponseEntity.ok(ApiResponse.success(Map.of(
                "status", "SUCCESS",
                "ticketId", ticketId,
                "subject", ticket.getSubject(),
                "textLength", text.length(),
                "vectorLength", vector.length
            )));

        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("FAILED: " + e.getMessage()));
        }
    }
}
