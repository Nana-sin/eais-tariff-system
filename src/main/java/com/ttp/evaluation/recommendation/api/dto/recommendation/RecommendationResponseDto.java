package com.ttp.evaluation.recommendation.api.dto.recommendation;

import com.ttp.evaluation.recommendation.domain.MeasureResult;
import com.ttp.evaluation.recommendation.domain.RecommendationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponseDto {
    private String requestId;
    private String tnVedCode;
    private String productName;
    private RecommendationStatus status;
    private Double totalScore;
    private String summary;
    private List<MeasureResult> measures;
    private Instant createdAt;
    private Instant completedAt;
}