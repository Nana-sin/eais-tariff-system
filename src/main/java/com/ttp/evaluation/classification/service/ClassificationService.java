package com.ttp.evaluation.classification.service;

import com.ttp.evaluation.classification.api.dto.ClassificationRequestDto;
import com.ttp.evaluation.classification.api.dto.ClassificationResponseDto;
import com.ttp.evaluation.classification.repository.ClassificationRequestRepository;
import com.ttp.evaluation.classification.repository.UserRepository;
import com.ttp.evaluation.classification.domain.ClassificationRequest;
import com.ttp.evaluation.classification.domain.RequestStatus;
import com.ttp.evaluation.classification.domain.User;
import com.ttp.evaluation.shared.KafkaProducerService;
import com.ttp.evaluation.shared.events.ClassificationRequestedEvent;
import com.ttp.evaluation.shared.events.NotificationEvent;
import com.ttp.evaluation.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClassificationService {

    private final ClassificationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducerService;

    public ClassificationResponseDto createRequest(ClassificationRequestDto dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        ClassificationRequest request = ClassificationRequest.builder()
                .user(user)
                .productName(dto.getProductName())
                .productDescription(dto.getProductDescription())
                .tnVedCode(dto.getTnVedCode())
                .status(RequestStatus.PENDING)
                .build();

        request = requestRepository.save(request);
        log.info("✅ Created classification request: {}", request.getId());

        // Отправка события в Kafka
        ClassificationRequestedEvent event = ClassificationRequestedEvent.builder()
                .requestId(request.getId())
                .userId(userId)
                .productName(dto.getProductName())
                .productDescription(dto.getProductDescription())
                .tnVedCode(dto.getTnVedCode())
                .timestamp(Instant.now())
                .build();

        kafkaProducerService.sendClassificationRequest(event);

        return toDto(request);
    }

    @Cacheable(value = "classificationRequests", key = "#id", unless = "#result == null")
    @Transactional(readOnly = true)
    public ClassificationResponseDto getRequest(Long id) {
        ClassificationRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClassificationRequest", "id", id));
        return toDto(request);
    }

    @Transactional(readOnly = true)
    public Page<ClassificationResponseDto> getUserRequests(Long userId, Pageable pageable) {
        return requestRepository.findByUserId(userId, pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ClassificationResponseDto> getRequestsByStatus(RequestStatus status, Pageable pageable) {
        return requestRepository.findByStatus(status, pageable)
                .map(this::toDto);
    }

    @CacheEvict(value = "classificationRequests", key = "#id")
    public ClassificationResponseDto approveRequest(Long id, Long expertId, String comment) {
        ClassificationRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClassificationRequest", "id", id));

        User expert = userRepository.findById(expertId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", expertId));

        request.setStatus(RequestStatus.APPROVED);
        request.setExpertComment(comment);
        request.setReviewedBy(expert);
        request.setReviewedAt(Instant.now());

        request = requestRepository.save(request);
        log.info("✅ Request {} approved by expert {}", id, expertId);

        // Отправка уведомления
        NotificationEvent notification = NotificationEvent.builder()
                .email(request.getUser().getEmail())
                .subject("Classification Approved")
                .message("Your classification request has been approved by an expert.")
                .timestamp(Instant.now())
                .build();

        kafkaProducerService.sendNotification(notification);

        return toDto(request);
    }

    @CacheEvict(value = "classificationRequests", key = "#id")
    public ClassificationResponseDto rejectRequest(Long id, Long expertId, String comment) {
        ClassificationRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClassificationRequest", "id", id));

        User expert = userRepository.findById(expertId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", expertId));

        request.setStatus(RequestStatus.REJECTED);
        request.setExpertComment(comment);
        request.setReviewedBy(expert);
        request.setReviewedAt(Instant.now());

        request = requestRepository.save(request);
        log.info("❌ Request {} rejected by expert {}", id, expertId);

        // Отправка уведомления
        NotificationEvent notification = NotificationEvent.builder()
                .email(request.getUser().getEmail())
                .subject("Classification Rejected")
                .message("Your classification request has been rejected: " + comment)
                .timestamp(Instant.now())
                .build();

        kafkaProducerService.sendNotification(notification);

        return toDto(request);
    }

    private ClassificationResponseDto toDto(ClassificationRequest request) {
        return ClassificationResponseDto.builder()
                .id(request.getId())
                .productName(request.getProductName())
                .productDescription(request.getProductDescription())
                .tnVedCode(request.getTnVedCode())
                .status(request.getStatus())
                .expertComment(request.getExpertComment())
                .confidenceScore(request.getConfidenceScore())
                .userId(request.getUser().getId())
                .userEmail(request.getUser().getEmail())
                .reviewedBy(request.getReviewedBy() != null ? request.getReviewedBy().getId() : null)
                .reviewedAt(request.getReviewedAt())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
