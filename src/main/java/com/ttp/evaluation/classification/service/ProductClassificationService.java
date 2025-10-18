package com.ttp.evaluation.classification.service;

import com.ttp.evaluation.classification.api.dto.ProductClassificationDto;
import com.ttp.evaluation.classification.domain.ProductClassification;
import com.ttp.evaluation.classification.repository.ProductClassificationRepository;
import com.ttp.evaluation.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для работы со справочником ТН ВЭД
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductClassificationService {

    private final ProductClassificationRepository classificationRepository;

    /**
     * Получить классификацию по коду ТН ВЭД
     */
    @Cacheable(value = "productClassifications", key = "#tnVedCode")
    public ProductClassificationDto getByCode(String tnVedCode) {
        ProductClassification classification = classificationRepository.findByTnVedCode(tnVedCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ProductClassification", "tnVedCode", tnVedCode));
        return toDto(classification);
    }

    /**
     * Поиск по префиксу кода (для автодополнения)
     */
    @Cacheable(value = "productClassificationsByPrefix", key = "#prefix")
    public List<ProductClassificationDto> searchByPrefix(String prefix) {
        return classificationRepository.findByCodePrefix(prefix).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить дочерние элементы
     */
    public List<ProductClassificationDto> getChildren(String parentCode) {
        return classificationRepository.findByParentCode(parentCode).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Поиск по описанию
     */
    public List<ProductClassificationDto> searchByDescription(String searchTerm) {
        return classificationRepository.searchByDescription(searchTerm).stream()
                .map(this::toDto)
                .limit(50) // Ограничиваем результаты
                .collect(Collectors.toList());
    }

    /**
     * Получить корневые элементы (главы - 2 знака)
     */
    @Cacheable(value = "rootClassifications", unless = "#result == null")
    public List<ProductClassificationDto> getRootClassifications() {
        return classificationRepository.findRootClassifications().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ProductClassificationDto toDto(ProductClassification classification) {
        return ProductClassificationDto.builder()
                .id(classification.getId())
                .tnVedCode(classification.getTnVedCode())
                .description(classification.getDescription())
                .parentCode(classification.getParentCode())
                .level(classification.getLevel())
                .active(classification.getActive())
                .build();
    }
}