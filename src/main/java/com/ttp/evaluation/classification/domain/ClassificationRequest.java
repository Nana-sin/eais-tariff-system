package com.ttp.evaluation.classification.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "classification_requests", indexes = {
        @Index(name = "idx_classification_user", columnList = "user_id"),
        @Index(name = "idx_classification_status", columnList = "status"),
        @Index(name = "idx_classification_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "classification_req_seq")
    @SequenceGenerator(name = "classification_req_seq", sequenceName = "classification_request_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "product_name", nullable = false, length = 1000)
    private String productName;

    @Column(name = "product_description", length = 5000)
    private String productDescription;

    @Column(name = "tn_ved_code", length = 10)
    private String tnVedCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RequestStatus status;

    @Column(name = "expert_comment", length = 5000)
    private String expertComment;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;
}

