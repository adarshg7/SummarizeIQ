package com.adarsh.aiPdfSummerizer.notification.config;

import org.springframework.context.annotation.Configuration;

/**
 * Mail configuration placeholder.
 * Spring Boot auto-configures JavaMailSender from application.properties
 * (spring.mail.*). Add @Bean overrides here only if you need custom behavior.
 */
@Configuration
public class MailConfig {
    // Spring Boot auto-configures JavaMailSender — no manual @Bean needed.
    // To disable mail entirely (e.g., in tests), set:
    //   spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration
}
