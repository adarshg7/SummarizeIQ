package com.adarsh.aiPdfSummerizer.document.controller;

import com.adarsh.aiPdfSummerizer.common.dto.ApiResponse;
import com.adarsh.aiPdfSummerizer.document.entity.Document;
import com.adarsh.aiPdfSummerizer.document.service.DocumentService;
import com.adarsh.aiPdfSummerizer.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;

    @PostMapping(value = "/upload",consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Long>> upload(@RequestParam("file") MultipartFile file){
        String email = SecurityUtils.getCurrentUserEmail();
        Document document = documentService.uploadAndExtract(file,email);

        return ResponseEntity.ok(ApiResponse.success("Files uploaded successfully",document.getId()));
    }

    @org.springframework.web.bind.annotation.GetMapping
    public ResponseEntity<ApiResponse<java.util.List<java.util.Map<String, Object>>>> listDocuments() {
        String email = SecurityUtils.getCurrentUserEmail();
        java.util.List<Document> docs = documentService.getUserDocuments(email);
        java.util.List<java.util.Map<String, Object>> response = docs.stream().map(d -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", d.getId());
            map.put("fileName", d.getFileName());
            return map;
        }).toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
