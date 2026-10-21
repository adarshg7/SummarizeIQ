package com.adarsh.aiPdfSummerizer.document.repository;

import com.adarsh.aiPdfSummerizer.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document,Long> {
    java.util.List<Document> findByUploadedByEmailOrderByIdDesc(String email);
}
