package com.ttp.evaluation.recommendation.api.dto.classification;

import com.ttp.evaluation.classification.domain.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationResponseDto {
    private Long id;
    private String productName;
    private String productDescription;
    private String tnVedCode;
    private RequestStatus status;
    private String expertComment;
    private Double confidenceScore;
    private Long userId;
    private String userEmail;
    private Long reviewedBy;
    private Instant reviewedAt;
    private Instant createdAt;
    private Instant updatedAt;
}