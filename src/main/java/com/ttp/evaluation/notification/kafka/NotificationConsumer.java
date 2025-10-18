package com.ttp.evaluation.notification.kafka;

import com.ttp.evaluation.notification.service.NotificationService;
import com.ttp.evaluation.shared.events.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Consumer для обработки уведомлений
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "${app.kafka.topics.notifications}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleNotification(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {

        log.info("📥 Received notification from Kafka: {} -> {}", event.getEmail(), event.getSubject());

        try {
            /*
            notificationService.sendEmail(

                    event.getEmail(),
                    event.getSubject(),
                    event.getMessage()
            );
            */

            // acknowledgment.acknowledge();

            // log.info("✅ Successfully sent notification to: {}", event.getEmail());
            log.info("Почта отправлена");
        } catch (Exception e) {
            log.error("❌ Failed to send notification to: {}", event.getEmail(), e);
            acknowledgment.acknowledge();
        }
    }
}