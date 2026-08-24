package com.adarsh.aiPdfSummerizer.embedding.entity;

import com.adarsh.aiPdfSummerizer.common.entity.BaseEntity;
import com.adarsh.aiPdfSummerizer.ticket.entity.Ticket;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ticket_embeddings")
public class TicketEmbedding extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    // The exact text that was embedded (subject + description combined)
    // kept alongside the vector so RAG can show WHY a ticket matched.
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String sourceText;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String vectorData; // comma-separated floats
}