package com.adarsh.aiPdfSummerizer.summary.service;

import com.adarsh.aiPdfSummerizer.common.exception.BadRequestException;
import com.adarsh.aiPdfSummerizer.document.entity.Document;
import com.adarsh.aiPdfSummerizer.document.service.DocumentService;
import com.adarsh.aiPdfSummerizer.queue.AiJob;
import com.adarsh.aiPdfSummerizer.queue.AiJobQueue;
import com.adarsh.aiPdfSummerizer.summary.dto.SummaryResponse;
import com.adarsh.aiPdfSummerizer.summary.entity.Summary;
import com.adarsh.aiPdfSummerizer.summary.entity.SummaryStatus;
import com.adarsh.aiPdfSummerizer.summary.repository.SummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // keeps session open for lazy loads; use readOnly for reads
public class SummaryService {

    private final SummaryRepository summaryRepository;
    private final DocumentService documentService;
    private final AiJobQueue aiJobQueue; // <-- Spring injects the ONE shared instance here

    @Transactional // overrides class-level readOnly=true — this method writes to DB
    public SummaryResponse summarize(Long documentId) {
        Document document = documentService.getById(documentId);

        if (document.getExtractedText() == null || document.getExtractedText().isBlank()) {
            throw new BadRequestException("Document has no extractable text");
        }

        Summary summary = new Summary();
        summary.setContent("");
        summary.setStatus(SummaryStatus.PENDING);
        summary.setDocument(document);
        Summary saved = summaryRepository.save(summary);

        String prompt = "Summarize the following document in a concise paragraph:\n\n"
                + document.getExtractedText();

        aiJobQueue.submit(new AiJob(
                prompt,
                result -> {
                    saved.setContent(result);
                    saved.setStatus(SummaryStatus.COMPLETED);
                    summaryRepository.save(saved);
                },
                error -> {
                    saved.setStatus(SummaryStatus.FAILED);
                    summaryRepository.save(saved);
                }
        ));

        return SummaryResponse.from(saved);
    }

    public SummaryResponse getByDocumentId(Long documentId) {
        Summary summary = summaryRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new BadRequestException("No summary exists for this document yet"));
        return SummaryResponse.from(summary);
    }
}