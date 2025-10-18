package com.ttp.evaluation.recommendation.api.controller;

import com.ttp.evaluation.recommendation.api.dto.recommendation.RecommendationRequestDto;
import com.ttp.evaluation.recommendation.api.dto.recommendation.RecommendationResponseDto;
import com.ttp.evaluation.recommendation.domain.RecommendationStatus;
import com.ttp.evaluation.recommendation.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
 * REST API для работы с рекомендациями мер ТТП
 */
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Recommendations", description = "API для оценки и рекомендации мер ТТП")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping
    @Operation(
            summary = "Создать оценку мер ТТП",
            description = "Запускает процесс оценки эффективности мер ТТП для указанного товара"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Оценка успешно создана",
                    content = @Content(schema = @Schema(implementation = RecommendationResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<RecommendationResponseDto> createRecommendation(
            @Valid @RequestBody RecommendationRequestDto request) {

        log.info("📥 Received recommendation request for TN VED: {}, User: {}",
                request.getTnVedCode(), request.getUserId());

        RecommendationResponseDto response = recommendationService.evaluateMeasures(request);

        log.info("✅ Recommendation created successfully: {}", response.getRequestId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{requestId}")
    @Operation(
            summary = "Получить оценку по ID",
            description = "Возвращает результаты оценки мер ТТП по уникальному идентификатору запроса"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Оценка найдена",
                    content = @Content(schema = @Schema(implementation = RecommendationResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Оценка не найдена")
    })
    public ResponseEntity<RecommendationResponseDto> getRecommendation(
            @Parameter(description = "Уникальный идентификатор запроса (UUID)")
            @PathVariable String requestId) {

        log.info("📤 Fetching recommendation: {}", requestId);

        RecommendationResponseDto response = recommendationService.getRecommendation(requestId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Получить оценки пользователя",
            description = "Возвращает список всех оценок мер ТТП для указанного пользователя"
    )
    public ResponseEntity<Page<RecommendationResponseDto>> getUserRecommendations(
            @Parameter(description = "ID пользователя")
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("📤 Fetching recommendations for user: {}", userId);

        // TODO: Implement in service
        return ResponseEntity.ok(Page.empty());
    }

    @GetMapping("/status/{status}")
    @Operation(
            summary = "Получить оценки по статусу",
            description = "Возвращает список оценок с указанным статусом"
    )
    public ResponseEntity<Page<RecommendationResponseDto>> getRecommendationsByStatus(
            @Parameter(description = "Статус оценки")
            @PathVariable RecommendationStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("📤 Fetching recommendations with status: {}", status);

        // TODO: Implement in service
        return ResponseEntity.ok(Page.empty());
    }
}