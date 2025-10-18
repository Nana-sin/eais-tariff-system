package com.ttp.evaluation.classification.repository;

import com.ttp.evaluation.classification.domain.ClassificationRequest;
import com.ttp.evaluation.classification.domain.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository для работы с запросами на классификацию
 */
@Repository
public interface ClassificationRequestRepository extends JpaRepository<ClassificationRequest, Long> {

    /**
     * Найти запросы пользователя с пагинацией
     */
    Page<ClassificationRequest> findByUserId(Long userId, Pageable pageable);

    /**
     * Найти запросы по статусу с пагинацией
     */
    Page<ClassificationRequest> findByStatus(RequestStatus status, Pageable pageable);

    /**
     * Найти запросы, ожидающие обработки (сортировка по дате создания)
     */
    @Query("SELECT cr FROM ClassificationRequest cr WHERE cr.status = :status ORDER BY cr.createdAt ASC")
    List<ClassificationRequest> findPendingRequests(@Param("status") RequestStatus status);

    /**
     * Подсчитать количество запросов по статусу
     */
    Long countByStatus(RequestStatus status);

    /**
     * Найти запросы пользователя по статусу
     */
    List<ClassificationRequest> findByUserIdAndStatus(Long userId, RequestStatus status);

    /**
     * Найти запросы, проверенные экспертом
     */
    @Query("SELECT cr FROM ClassificationRequest cr WHERE cr.reviewedBy.id = :expertId")
    Page<ClassificationRequest> findReviewedByExpert(@Param("expertId") Long expertId, Pageable pageable);

    /**
     * Найти запросы за период
     */
    @Query("SELECT cr FROM ClassificationRequest cr WHERE cr.createdAt BETWEEN :startDate AND :endDate")
    List<ClassificationRequest> findByDateRange(@Param("startDate") Instant startDate,
                                                @Param("endDate") Instant endDate);

    /**
     * Найти последние N запросов пользователя
     */
    List<ClassificationRequest> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
}
