package com.adarsh.aiPdfSummerizer.scheduler;

import com.adarsh.aiPdfSummerizer.ticket.entity.TicketStatus;
import com.adarsh.aiPdfSummerizer.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Runs once a day at midnight, logs a simple daily ticket summary.
 * Deliberately simple for now — a real "generate report" feature
 * (PDF export, emailed report) would build on top of this later,
 * but the scheduled trigger + basic counts is the right starting point.
 */
@Component
@RequiredArgsConstructor
public class TicketReportJob {

    private static final Logger log = LoggerFactory.getLogger(TicketReportJob.class);

    private final TicketRepository ticketRepository;

    @Scheduled(cron = "0 0 0 * * *") // midnight every day
    public void generateDailyReport() {
        long open = ticketRepository.countByStatus(TicketStatus.OPEN);
        long inProgress = ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);
        long closed = ticketRepository.countByStatus(TicketStatus.CLOSED);

        log.info("Daily ticket report — OPEN: {}, IN_PROGRESS: {}, CLOSED: {}", open, inProgress, closed);
    }
}