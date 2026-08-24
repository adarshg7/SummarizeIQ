package com.adarsh.aiPdfSummerizer.cache;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.Duration;
import java.util.Optional;

/**
 * Wraps Redis specifically for caching AI responses. Every OTHER
 * class in the app (ChatService) calls THIS, never RedisTemplate
 * directly — same boundary pattern as AiClient/NotificationService.
 * If we ever swap Redis for another cache store, this is the only
 * file that changes.
 */
@Service
@RequiredArgsConstructor
public class AiResponseCacheService {

    private static final Logger log = LoggerFactory.getLogger(AiResponseCacheService.class);
    private static final String KEY_PREFIX = "ai:response:";
    private static final Duration TTL = Duration.ofHours(6); // cached answers expire after 6h

    private final RedisTemplate<String, Object> redisTemplate;

    public Optional<String> get(String prompt) {
        String key = buildKey(prompt);
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.info("Cache HIT for prompt hash: {}", key);
        }
        return Optional.ofNullable((String) cached);
    }

    public void put(String prompt, String response) {
        String key = buildKey(prompt);
        redisTemplate.opsForValue().set(key, response, TTL);
    }

    // Hash the prompt rather than using it directly as the Redis key:
    // prompts can be long/contain special characters, and hashing gives
    // a short, consistent, safe key regardless of input length.
    private String buildKey(String prompt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(prompt.trim().toLowerCase().getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return KEY_PREFIX + hex;
        } catch (Exception e) {
            // Fallback: still works, just less collision-safe. Never let
            // caching itself become a reason the AI call fails.
            return KEY_PREFIX + prompt.trim().toLowerCase().hashCode();
        }
    }
}