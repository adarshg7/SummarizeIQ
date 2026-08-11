package com.adarsh.aiPdfSummerizer.queue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {

    @Bean(name = "aiTaskExecutor")
    public TaskExecutor aiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);   // always-alive worker threads
        executor.setMaxPoolSize(10);   // burst capacity under load
        executor.setQueueCapacity(100); // requests waiting for a free worker
        executor.setThreadNamePrefix("AI-Worker-");
        executor.initialize();
        return executor;
    }
}
