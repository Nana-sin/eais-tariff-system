package com.ttp.evaluation.recommendation.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "measure_results", indexes = {
        @Index(name = "idx_measure_recommendation", columnList = "recommendation_id"),
        @Index(name = "idx_measure_type", columnList = "measure_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeasureResult {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "measure_result_seq")
    @SequenceGenerator(name = "measure_result_seq", sequenceName = "measure_result_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private TtpRecommendation recommendation;

    @Enumerated(EnumType.STRING)
    @Column(name = "measure_type", nullable = false, length = 50)
    private MeasureType measureType;

    @Column(name = "measure_name", nullable = false, length = 255)
    private String measureName;

    @Column(nullable = false)
    private Boolean applicable;

    @Column(nullable = false)
    private Double score;

    @Column(length = 2000)
    private String reasoning;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "import_share")
    private Double importShare;

    @Column(name = "production_capacity")
    private Double productionCapacity;

    @Column(name = "price_difference")
    private Double priceDifference;
}