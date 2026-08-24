package com.adarsh.aiPdfSummerizer.embedding.service;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Indexes content for future RAG (semantic) search.
 * Supports two content types:
 *   - Tickets  → for answering "what were my past support issues?"
 *   - Documents → for answering "what does my PDF say about X?"
 *
 * Both run @Async — the caller is never blocked by embedding work.
 */
@Service
@RequiredArgsConstructor
public class EmbeddingIndexService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingIndexService.class);

    private final EmbeddingClient embeddingClient;
    private final TicketEmbeddingRepository ticketEmbeddingRepository;
    private final DocumentEmbeddingRepository documentEmbeddingRepository;
    private final DocumentRepository documentRepository;
    private final TicketRepository ticketRepository;

    // ── Ticket indexing ────────────────────────────────────────────────────────

    @Async("aiTaskExecutor")
    public void indexTicket(Long ticketId) {
        try {
            Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
            if (ticket == null) {
                log.warn("[EmbeddingService] Ticket {} not found in DB", ticketId);
                return;
            }

            String sourceText = ticket.getSubject() + ". " + ticket.getDescription();
            log.info("[EmbeddingService] Generating embedding for Ticket {}...", ticketId);
            float[] vector = embeddingClient.embed(sourceText);

            TicketEmbedding embedding = new TicketEmbedding();
            embedding.setTicket(ticket);
            embedding.setSourceText(sourceText);
            embedding.setVectorData(VectorMathUtil.toStorageString(vector));

            ticketEmbeddingRepository.save(embedding);
            log.info("[EmbeddingService] Successfully saved embedding for Ticket {}", ticketId);
        } catch (Exception e) {
            log.error("[EmbeddingService] FAILED to generate embedding for Ticket {}: {}", ticketId, e.getMessage(), e);
        }
    }

    // ── PDF Document indexing ──────────────────────────────────────────────────

    @Async("aiTaskExecutor")
    public void indexDocument(Long documentId) {
        try {
            // Reload fresh from DB inside async thread to avoid JPA detachment issues
            Document document = documentRepository.findById(documentId).orElse(null);
            if (document == null) {
                log.warn("[EmbeddingService] Document {} not found in DB", documentId);
                return;
            }

            String textToEmbed = document.getExtractedText();
            if (textToEmbed == null || textToEmbed.isBlank()) {
                log.warn("[EmbeddingService] Document {} has no text to embed", documentId);
                return;
            }

            // Skip if already indexed
            if (documentEmbeddingRepository.findByDocument_Id(documentId).isPresent()) {
                log.info("[EmbeddingService] Document {} already indexed, skipping", documentId);
                return;
            }

            log.info("[EmbeddingService] Generating embeddings for Document {}...", documentId);
            float[] vector = embeddingClient.embed(textToEmbed);

            DocumentEmbedding embedding = new DocumentEmbedding();
            embedding.setDocument(document);
            embedding.setSourceText(textToEmbed);
            embedding.setVectorData(VectorMathUtil.toStorageString(vector));

            documentEmbeddingRepository.save(embedding);
            log.info("[EmbeddingService] Successfully saved embedding for Document {}", documentId);
        } catch (Exception e) {
            log.error("[EmbeddingService] FAILED to generate embedding for Document {}: {}", documentId, e.getMessage(), e);
        }
    }
}