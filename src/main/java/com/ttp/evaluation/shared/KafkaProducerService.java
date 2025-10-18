package com.ttp.evaluation.shared;

import com.ttp.evaluation.shared.events.ClassificationRequestedEvent;
import com.ttp.evaluation.shared.events.NotificationEvent;
import com.ttp.evaluation.shared.events.TtpEvaluationRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Сервис для отправки событий в Kafka
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.classification-requests}")
    private String classificationRequestsTopic;

    @Value("${app.kafka.topics.ttp-evaluation-tasks}")
    private String ttpEvaluationTasksTopic;

    @Value("${app.kafka.topics.notifications}")
    private String notificationsTopic;

    /**
     * Отправка события о создании запроса на классификацию
     */
    public void sendClassificationRequest(ClassificationRequestedEvent event) {
        log.info("📤 Sending classification request event to Kafka: {}", event.getRequestId());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                classificationRequestsTopic,
                event.getRequestId().toString(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ Classification request event sent successfully: {}", event.getRequestId());
            } else {
                log.error("❌ Failed to send classification request event: {}", event.getRequestId(), ex);
            }
        });
    }

    /**
     * Отправка события о запросе оценки мер ТТП
     */
    public void sendTtpEvaluationRequest(TtpEvaluationRequestedEvent event) {
        log.info("📤 Sending TTP evaluation request event to Kafka: {}", event.getRequestId());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                ttpEvaluationTasksTopic,
                event.getRequestId(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ TTP evaluation request event sent successfully: {}", event.getRequestId());
            } else {
                log.error("❌ Failed to send TTP evaluation request event: {}", event.getRequestId(), ex);
            }
        });
    }

    /**
     * Отправка уведомления пользователю
     */
    public void sendNotification(NotificationEvent event) {
        log.info("📤 Sending notification event to Kafka: {} -> {}", event.getEmail(), event.getSubject());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                notificationsTopic,
                event.getEmail(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ Notification event sent successfully to: {}", event.getEmail());
            } else {
                log.error("❌ Failed to send notification event to: {}", event.getEmail(), ex);
            }
        });
    }
}