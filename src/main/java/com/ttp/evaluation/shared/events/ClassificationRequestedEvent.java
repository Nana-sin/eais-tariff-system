package com.ttp.evaluation.shared.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Событие: Создан запрос на классификацию
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationRequestedEvent {
    private Long requestId;
    private Long userId;
    private String productName;
    private String productDescription;
    private String tnVedCode;
    private Instant timestamp;
}