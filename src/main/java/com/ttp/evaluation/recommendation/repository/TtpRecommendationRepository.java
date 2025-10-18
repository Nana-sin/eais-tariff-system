package com.ttp.evaluation.recommendation.repository;

import com.ttp.evaluation.recommendation.domain.RecommendationStatus;
import com.ttp.evaluation.recommendation.domain.TtpRecommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TtpRecommendationRepository extends JpaRepository<TtpRecommendation, Long> {

    Optional<TtpRecommendation> findByRequestId(String requestId);

    Page<TtpRecommendation> findByUserId(Long userId, Pageable pageable);

    Page<TtpRecommendation> findByStatus(RecommendationStatus status, Pageable pageable);

    @Query("SELECT r FROM TtpRecommendation r WHERE r.tnVedCode = :tnVedCode ORDER BY r.createdAt DESC")
    List<TtpRecommendation> findByTnVedCode(@Param("tnVedCode") String tnVedCode, Pageable pageable);

    @Query("SELECT r FROM TtpRecommendation r WHERE r.status = :status AND r.createdAt >= :since")
    List<TtpRecommendation> findRecentByStatus(@Param("status") RecommendationStatus status,
                                               @Param("since") Instant since);

    Long countByStatus(RecommendationStatus status);
}