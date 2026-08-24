package com.adarsh.aiPdfSummerizer.notification;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final JavaMailSender mailSender;

    @Async  // uses Spring's default async executor — not the AI worker pool
    public void sendEmail(String to, String subject, String body){
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {}",to);
        } catch (Exception e){
            log.error("Failed to send remail to {}:{}",to,e.getMessage());
        }
    }

    public void sendTicketCreatedEmail(String toEmail, String customerName, Long ticketId, String subject){
        String body = String.format(
                "Hi %s,%n%nYour support ticket #%d (\"%s\") has been created.%n%n" +
                        "Our team will get back to you shortly.%n%nThanks,",
                customerName, ticketId, subject
        );
        sendEmail(toEmail, "Ticket created - #" + ticketId, body);
    }

    public void sendTicketClosedEmail(String toEmail, String customerName, Long ticketId){
        String body = String.format(
                "Hi %s,%n%nYour support ticket #%d has been marked as resolved and closed. " +
                        "If you still need help, feel free to reopen it or start a new chat.%n%nThanks.",
                customerName,ticketId
        );
        sendEmail(toEmail, "Ticket Closed - #" + ticketId, body);
    }
}
