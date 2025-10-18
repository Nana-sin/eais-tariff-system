package com.ttp.evaluation.classification.api.controller;

import com.ttp.evaluation.classification.api.dto.ClassificationRequestDto;
import com.ttp.evaluation.classification.api.dto.ClassificationResponseDto;
import com.ttp.evaluation.classification.service.ClassificationService;
import com.ttp.evaluation.classification.domain.RequestStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API для работы с классификацией товаров
 */
@RestController
@RequestMapping("/api/v1/classifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Classifications", description = "API для классификации товаров по кодам ТН ВЭД")
public class ClassificationController {

    private final ClassificationService classificationService;

    @PostMapping
    @Operation(
            summary = "Создать запрос на классификацию",
            description = "Создает новый запрос на классификацию товара по коду ТН ВЭД"
    )
    public ResponseEntity<ClassificationResponseDto> createClassificationRequest(
            @Valid @RequestBody ClassificationRequestDto request,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("📥 Creating classification request for user: {}", userId);

        ClassificationResponseDto response = classificationService.createRequest(request, userId);

        log.info("✅ Classification request created: {}", response.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить запрос на классификацию",
            description = "Возвращает запрос на классификацию по ID"
    )
    public ResponseEntity<ClassificationResponseDto> getClassificationRequest(
            @PathVariable Long id) {

        log.info("📤 Fetching classification request: {}", id);

        ClassificationResponseDto response = classificationService.getRequest(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Получить запросы пользователя",
            description = "Возвращает список всех запросов на классификацию для пользователя"
    )
    public ResponseEntity<Page<ClassificationResponseDto>> getUserRequests(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("📤 Fetching classification requests for user: {}", userId);

        Page<ClassificationResponseDto> response = classificationService.getUserRequests(userId, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(
            summary = "Получить запросы по статусу",
            description = "Возвращает список запросов с указанным статусом"
    )
    public ResponseEntity<Page<ClassificationResponseDto>> getRequestsByStatus(
            @PathVariable RequestStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("📤 Fetching classification requests with status: {}", status);

        Page<ClassificationResponseDto> response = classificationService.getRequestsByStatus(status, pageable);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/approve")
    @Operation(
            summary = "Одобрить классификацию",
            description = "Эксперт одобряет классификацию товара"
    )
    public ResponseEntity<ClassificationResponseDto> approveRequest(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long expertId,
            @RequestParam(required = false) String comment) {

        log.info("✅ Approving classification request: {} by expert: {}", id, expertId);

        ClassificationResponseDto response = classificationService.approveRequest(id, expertId, comment);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reject")
    @Operation(
            summary = "Отклонить классификацию",
            description = "Эксперт отклоняет классификацию товара"
    )
    public ResponseEntity<ClassificationResponseDto> rejectRequest(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long expertId,
            @RequestParam String comment) {

        log.info("❌ Rejecting classification request: {} by expert: {}", id, expertId);

        ClassificationResponseDto response = classificationService.rejectRequest(id, expertId, comment);

        return ResponseEntity.ok(response);
    }
}