package com.adarsh.aiPdfSummerizer.embedding.repository;

import com.adarsh.aiPdfSummerizer.embedding.entity.DocumentEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentEmbeddingRepository extends JpaRepository<DocumentEmbedding, Long> {

    // Find all embeddings for documents uploaded by a specific user — for RAG search
    List<DocumentEmbedding> findByDocument_UploadedBy_Id(Long userId);

    // Check if a document has already been indexed
    Optional<DocumentEmbedding> findByDocument_Id(Long documentId);
}
