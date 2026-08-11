package com.adarsh.aiPdfSummerizer.queue;

import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class AiJobQueue {
    private final BlockingQueue<AiJob> queue = new LinkedBlockingQueue<>(500);
    public void submit(AiJob job){
        try{
            queue.put(job);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to queue AI job");
        }
    }

    public AiJob take() throws InterruptedException {
        return queue.take();
    }
}
