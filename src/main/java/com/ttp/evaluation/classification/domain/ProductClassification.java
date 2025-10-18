package com.ttp.evaluation.classification.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Справочник классификации товаров (ТН ВЭД)
 */
@Entity
@Table(name = "product_classifications", indexes = {
        @Index(name = "idx_product_tn_ved", columnList = "tn_ved_code"),
        @Index(name = "idx_product_parent", columnList = "parent_code"),
        @Index(name = "idx_product_level", columnList = "level")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductClassification {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_class_seq")
    @SequenceGenerator(name = "product_class_seq", sequenceName = "product_classification_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "tn_ved_code", nullable = false, unique = true, length = 10)
    private String tnVedCode;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "parent_code", length = 10)
    private String parentCode;

    @Column(nullable = false)
    private Integer level; // 2, 4, 6, 8, 10 знаков

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;
}