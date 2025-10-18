package com.ttp.evaluation.shared.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Событие: Отправка уведомления пользователю
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String email;
    private String subject;
    private String message;
    private NotificationType type;
    private Instant timestamp;
}

enum NotificationType {
    EMAIL,
    SMS,
    PUSH
}