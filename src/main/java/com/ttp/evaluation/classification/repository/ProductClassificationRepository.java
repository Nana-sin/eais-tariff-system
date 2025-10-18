package com.ttp.evaluation.classification.repository;

import com.ttp.evaluation.classification.domain.ProductClassification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository для работы со справочником ТН ВЭД
 */
@Repository
public interface ProductClassificationRepository extends JpaRepository<ProductClassification, Long> {

    /**
     * Найти классификацию по коду ТН ВЭД
     */
    Optional<ProductClassification> findByTnVedCode(String tnVedCode);

    /**
     * Найти дочерние элементы по родительскому коду
     */
    List<ProductClassification> findByParentCode(String parentCode);

    /**
     * Найти все классификации с префиксом кода
     */
    @Query("SELECT pc FROM ProductClassification pc WHERE pc.tnVedCode LIKE :prefix% AND pc.active = true")
    List<ProductClassification> findByCodePrefix(@Param("prefix") String prefix);

    /**
     * Найти классификации по уровню иерархии
     */
    List<ProductClassification> findByLevel(Integer level);

    /**
     * Найти активные классификации по уровню
     */
    List<ProductClassification> findByLevelAndActiveTrue(Integer level);

    /**
     * Поиск по описанию (полнотекстовый поиск)
     */
    @Query("SELECT pc FROM ProductClassification pc WHERE LOWER(pc.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND pc.active = true")
    List<ProductClassification> searchByDescription(@Param("searchTerm") String searchTerm);

    /**
     * Найти все активные корневые элементы (2 знака)
     */
    @Query("SELECT pc FROM ProductClassification pc WHERE pc.level = 2 AND pc.active = true ORDER BY pc.tnVedCode")
    List<ProductClassification> findRootClassifications();

    /**
     * Проверить существование кода
     */
    boolean existsByTnVedCode(String tnVedCode);
}