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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // keeps session open for lazy loads; use readOnly for reads
public class SummaryService {

    private static final Logger log = LoggerFactory.getLogger(SummaryService.class);

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

        Long savedId = saved.getId(); // capture only the ID — entity will be detached after tx ends

        aiJobQueue.submit(new AiJob(
                prompt,
                result -> {
                    // Re-fetch inside the worker thread — each repo.save() manages its own tx
                    summaryRepository.findById(savedId).ifPresent(s -> {
                        s.setContent(result);
                        s.setStatus(SummaryStatus.COMPLETED);
                        summaryRepository.save(s);
                    });
                },
                error -> {
                    log.error("[SummaryService] AI job FAILED for summaryId={} — {}", savedId, error.getMessage(), error);
                    summaryRepository.findById(savedId).ifPresent(s -> {
                        s.setStatus(SummaryStatus.FAILED);
                        summaryRepository.save(s);
                    });
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