package com.ttp.evaluation.recommendation.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ttp_recommendations", indexes = {
        @Index(name = "idx_recommendation_request_id", columnList = "request_id"),
        @Index(name = "idx_recommendation_user", columnList = "user_id"),
        @Index(name = "idx_recommendation_status", columnList = "status"),
        @Index(name = "idx_recommendation_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TtpRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ttp_recommendation_seq")
    @SequenceGenerator(name = "ttp_recommendation_seq", sequenceName = "ttp_recommendation_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "request_id", nullable = false, unique = true, length = 36)
    private String requestId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "tn_ved_code", nullable = false, length = 10)
    private String tnVedCode;

    @Column(name = "product_name", nullable = false, length = 1000)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RecommendationStatus status;

    @OneToMany(mappedBy = "recommendation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<MeasureResult> measures = new ArrayList<>();

    @Column(length = 5000)
    private String summary;

    @Column(name = "total_score")
    private Double totalScore;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Version
    private Long version;
}
