package com.adarsh.aiPdfSummerizer.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Your spec's "retrain FAQ cache" — in practice here, that means
 * clearing stale AI response cache entries periodically so answers
 * don't silently go stale beyond what the TTL alone guarantees, and
 * so memory doesn't grow unbounded with rarely-reused questions.
 */
@Component
public class FaqCacheRefreshJob {

    private static final Logger log = LoggerFactory.getLogger(FaqCacheRefreshJob.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public FaqCacheRefreshJob(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(cron = "0 0 3 * * *") // 3 AM daily — low-traffic window
    public void clearStaleFaqCache() {
        Set<String> keys = redisTemplate.keys("ai:response:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("FAQ cache refresh: cleared {} cached AI response(s)", keys.size());
        } else {
            log.info("FAQ cache refresh: nothing to clear");
        }
    }
}