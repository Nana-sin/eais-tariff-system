package com.ttp.evaluation.recommendation.kafka;

import com.ttp.evaluation.recommendation.api.dto.recommendation.RecommendationRequestDto;
import com.ttp.evaluation.recommendation.service.RecommendationService;
import com.ttp.evaluation.shared.events.TtpEvaluationRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Consumer для обработки запросов на оценку мер ТТП
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TtpEvaluationConsumer {

    private final RecommendationService recommendationService;

    @KafkaListener(
            topics = "${app.kafka.topics.ttp-evaluation-tasks}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleTtpEvaluationRequest(
            @Payload TtpEvaluationRequestedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("📥 Received TTP evaluation request from Kafka: {} (topic: {}, partition: {}, offset: {})",
                event.getRequestId(), topic, partition, offset);

        try {
            RecommendationRequestDto request = RecommendationRequestDto.builder()
                    .userId(event.getUserId())
                    .tnVedCode(event.getTnVedCode())
                    .productName(event.getProductName())
                    .build();

            recommendationService.evaluateMeasures(request);

            // Подтверждаем обработку сообщения
            acknowledgment.acknowledge();

            log.info("✅ Successfully processed TTP evaluation request: {}", event.getRequestId());

        } catch (Exception e) {
            log.error("❌ Failed to process TTP evaluation request: {}", event.getRequestId(), e);
            // TODO: Implement retry logic or send to DLQ (Dead Letter Queue)
            acknowledgment.acknowledge(); // Подтверждаем чтобы не зациклиться
        }
    }
}