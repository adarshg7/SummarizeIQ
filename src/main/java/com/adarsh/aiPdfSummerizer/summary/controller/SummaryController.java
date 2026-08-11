package com.adarsh.aiPdfSummerizer.summary.controller;

import com.adarsh.aiPdfSummerizer.common.dto.ApiResponse;
import com.adarsh.aiPdfSummerizer.summary.dto.SummaryResponse;
import com.adarsh.aiPdfSummerizer.summary.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
public class SummaryController {
    private final SummaryService summaryService;
    @PostMapping("/{documentId}")
    public ResponseEntity<ApiResponse<SummaryResponse>> summarize(@PathVariable Long documentId){
        SummaryResponse response = summaryService.summarize(documentId);
        return ResponseEntity.ok(ApiResponse.success("Document summarized successfully", response));
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<ApiResponse<SummaryResponse>> getSummary(@PathVariable Long documentId){
        SummaryResponse response = summaryService.getByDocumentId(documentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
