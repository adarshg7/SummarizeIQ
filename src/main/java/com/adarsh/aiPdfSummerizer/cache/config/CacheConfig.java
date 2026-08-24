package com.adarsh.aiPdfSummerizer.cache.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configures HOW data is stored in Redis. Without a custom template,
 * Spring's default RedisTemplate serializes keys/values as raw Java
 * bytes — unreadable if you inspect Redis directly (e.g. via redis-cli),
 * and brittle if the Java class shape changes later. We explicitly use
 * String keys + JSON values instead, which is readable and portable.
 */
@Configuration
public class CacheConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(new ObjectMapper()));
        return template;
    }
}