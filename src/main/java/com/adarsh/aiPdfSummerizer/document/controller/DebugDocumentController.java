package com.adarsh.aiPdfSummerizer.document.controller;

import com.adarsh.aiPdfSummerizer.common.dto.ApiResponse;
import com.adarsh.aiPdfSummerizer.document.entity.Document;
import com.adarsh.aiPdfSummerizer.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/debug/documents")
@RequiredArgsConstructor
public class DebugDocumentController {
    
    private final DocumentService documentService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDocumentDebug(@PathVariable Long id) {
        Document doc = documentService.getById(id);
        
        Map<String, Object> debugInfo = Map.of(
            "documentId", doc.getId(),
            "fileName", doc.getFileName(),
            "extractedTextLength", doc.getExtractedText() != null ? doc.getExtractedText().length() : 0,
            "hasText", doc.getExtractedText() != null && !doc.getExtractedText().isBlank()
        );
            
        return ResponseEntity.ok(ApiResponse.success(debugInfo));
    }
}
