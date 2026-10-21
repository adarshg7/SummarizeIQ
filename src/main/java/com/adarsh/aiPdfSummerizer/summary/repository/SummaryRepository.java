package com.adarsh.aiPdfSummerizer.summary.repository;

import com.adarsh.aiPdfSummerizer.summary.entity.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SummaryRepository extends JpaRepository<Summary,Long> {
    Optional<Summary> findTopByDocumentIdOrderByIdDesc(Long documentId);
    Optional<Summary> findByDocumentId(Long documentId);
}
