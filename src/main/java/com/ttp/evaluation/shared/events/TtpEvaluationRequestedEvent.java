package com.ttp.evaluation.shared.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Событие: Запрошена оценка мер ТТП
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TtpEvaluationRequestedEvent {
    private String requestId;
    private Long userId;
    private String tnVedCode;
    private String productName;
    private Instant timestamp;
}