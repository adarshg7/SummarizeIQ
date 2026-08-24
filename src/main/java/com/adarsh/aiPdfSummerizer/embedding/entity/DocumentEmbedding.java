package com.adarsh.aiPdfSummerizer.embedding.entity;

import com.adarsh.aiPdfSummerizer.common.entity.BaseEntity;
import com.adarsh.aiPdfSummerizer.document.entity.Document;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "document_embeddings")
public class DocumentEmbedding extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", unique = true)
    private Document document;

    // The text that was actually embedded (extracted PDF text).
    // Stored alongside the vector so RAG can show WHY a document matched.
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String sourceText;

    // Comma-separated float values — the embedding vector from Gemini.
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String vectorData;
}
