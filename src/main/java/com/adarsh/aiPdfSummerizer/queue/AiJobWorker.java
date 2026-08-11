package com.adarsh.aiPdfSummerizer.queue;

import com.adarsh.aiPdfSummerizer.ai.AiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class AiJobWorker implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(AiJobWorker.class);
    private static final int WORKER_COUNT = 4;

    private final AiJobQueue aiJobQueue;
    private final AiClient aiClient;
    private final TaskExecutor taskExecutor;

    private volatile boolean running = false;

    public AiJobWorker(
            AiJobQueue aiJobQueue,
            AiClient aiClient,
            @Qualifier("aiTaskExecutor") TaskExecutor taskExecutor) {
        this.aiJobQueue = aiJobQueue;
        this.aiClient = aiClient;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void start() {
        if (this.running) {
            return;
        }
        this.running = true;

        log.info("Starting {} AI Job Workers...", WORKER_COUNT);
        for (int i = 0; i < WORKER_COUNT; i++) {
            int workerId = i + 1;
            taskExecutor.execute(() -> runWorkerLoop(workerId));
        }
    }

    private void runWorkerLoop(int workerId) {
        log.info("Worker-{} started.", workerId);
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                AiJob job = aiJobQueue.take();
                try {
                    String result = aiClient.generateResponse(job.getPrompt());
                    job.complete(result);
                } catch (Exception e) {
                    log.error("Worker-{} failed to process AI job", workerId, e);
                    job.fail(e);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Worker-{} interrupted, stopping loop", workerId);
                break;
            }
        }
        log.info("Worker-{} stopped.", workerId);
    }

    @Override
    public void stop() {
        this.running = false;
        log.info("Stopping AI Job Workers...");
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }
}