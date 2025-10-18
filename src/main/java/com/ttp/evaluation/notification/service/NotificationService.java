package com.ttp.evaluation.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Сервис отправки уведомлений
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    /**
     * Отправка email уведомления
     */
    public void sendEmail(String to, String subject, String text) {
        log.info("📧 Sending email to: {}, subject: {}", to, subject);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("noreply@ttp-evaluation.ru");

            mailSender.send(message);

            log.info("✅ Email sent successfully to: {}", to);

        } catch (Exception e) {
            log.error("❌ Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}