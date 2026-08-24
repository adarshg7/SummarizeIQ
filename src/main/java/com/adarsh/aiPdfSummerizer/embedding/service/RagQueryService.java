package com.adarsh.aiPdfSummerizer.embedding.service;

import com.adarsh.aiPdfSummerizer.ai.AiClient;
import com.adarsh.aiPdfSummerizer.embedding.EmbeddingClient;
import com.adarsh.aiPdfSummerizer.embedding.VectorMathUtil;
import com.adarsh.aiPdfSummerizer.embedding.entity.DocumentEmbedding;
import com.adarsh.aiPdfSummerizer.embedding.entity.TicketEmbedding;
import com.adarsh.aiPdfSummerizer.embedding.repository.DocumentEmbeddingRepository;
import com.adarsh.aiPdfSummerizer.embedding.repository.TicketEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * RAG (Retrieval-Augmented Generation) query service.
 *
 * Supports two RAG modes:
 *  1. Ticket RAG  — "What were my past support issues related to X?"
 *  2. PDF RAG     — "What does my uploaded PDF say about X?"
 *
 * Flow for both:
 *   Embed the question → cosine similarity against stored vectors
 *   → feed top-K matches as context to Groq AI → grounded answer
 */
@Service
@RequiredArgsConstructor
public class RagQueryService {

    private static final int TOP_K = 3;

    private final EmbeddingClient embeddingClient;
    private final TicketEmbeddingRepository ticketEmbeddingRepository;
    private final DocumentEmbeddingRepository documentEmbeddingRepository;
    private final AiClient aiClient;

    // ── Ticket RAG ─────────────────────────────────────────────────────────────

    public String summarizePreviousTickets(Long customerId, String userQuestion) {
        List<TicketEmbedding> allEmbeddings =
                ticketEmbeddingRepository.findByTicket_Customer_Id(customerId);

        if (allEmbeddings.isEmpty()) {
            return "You have no previous resolved tickets to summarize.";
        }

        float[] queryVector = embeddingClient.embed(userQuestion);

        List<TicketEmbedding> topMatches = allEmbeddings.stream()
                .sorted(Comparator.comparingDouble((TicketEmbedding e) ->
                        -VectorMathUtil.cosineSimilarity(
                                queryVector,
                                VectorMathUtil.fromStorageString(e.getVectorData())
                        )
                ))
                .limit(TOP_K)
                .toList();

        StringBuilder context = new StringBuilder();
        for (TicketEmbedding match : topMatches) {
            context.append("- ").append(match.getSourceText()).append("\n");
        }

        String prompt = "Here are the customer's most relevant past support tickets:\n"
                + context
                + "\nBased on this, answer the customer's question: " + userQuestion;

        return aiClient.generateResponse(prompt);
    }

    // ── PDF Document RAG ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public String answerFromPdfs(Long userId, String userQuestion) {
        List<DocumentEmbedding> allEmbeddings =
                documentEmbeddingRepository.findByDocument_UploadedBy_Id(userId);

        if (allEmbeddings.isEmpty()) {
            return "You have no uploaded PDFs to search. Please upload a PDF first.";
        }

        float[] queryVector = embeddingClient.embed(userQuestion);

        List<DocumentEmbedding> topMatches = allEmbeddings.stream()
                .sorted(Comparator.comparingDouble((DocumentEmbedding e) ->
                        -VectorMathUtil.cosineSimilarity(
                                queryVector,
                                VectorMathUtil.fromStorageString(e.getVectorData())
                        )
                ))
                .limit(TOP_K)
                .toList();

        StringBuilder context = new StringBuilder();
        for (DocumentEmbedding match : topMatches) {
            String fileName = match.getDocument().getFileName();
            String excerpt = match.getSourceText().length() > 2000
                    ? match.getSourceText().substring(0, 2000) + "..."
                    : match.getSourceText();
            context.append("--- ").append(fileName).append(" ---\n")
                   .append(excerpt).append("\n\n");
        }

        String prompt = "You are an assistant that answers questions strictly based on the provided PDF documents.\n\n"
                + "Relevant PDF content:\n" + context
                + "\nUser's question: " + userQuestion
                + "\n\nAnswer based only on the above documents. If the answer is not found, say so.";

        return aiClient.generateResponse(prompt);
    }
}