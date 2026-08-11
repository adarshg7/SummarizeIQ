package com.adarsh.aiPdfSummerizer.summary.dto;

import com.adarsh.aiPdfSummerizer.summary.entity.Summary;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SummaryResponse {
    private Long id;
    private String content;
    private String fileName;
    private String status;

    public static SummaryResponse from(Summary summary){
        return new SummaryResponse(
                summary.getId(),
                summary.getContent(),
                summary.getDocument().getFileName(),
                summary.getStatus().name()
        );
    }
}
