package com.ttp.evaluation.classification.api.controller;

import com.ttp.evaluation.classification.api.dto.ProductClassificationDto;
import com.ttp.evaluation.classification.service.ProductClassificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API для работы со справочником ТН ВЭД
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Classifications", description = "API для работы со справочником ТН ВЭД")
public class ProductClassificationController {

    private final ProductClassificationService productClassificationService;

    @GetMapping("/code/{tnVedCode}")
    @Operation(
            summary = "Получить классификацию по коду",
            description = "Возвращает информацию о товаре по коду ТН ВЭД"
    )
    public ResponseEntity<ProductClassificationDto> getByCode(
            @Parameter(description = "Код ТН ВЭД (2-10 знаков)")
            @PathVariable String tnVedCode) {

        log.info("📤 Fetching product classification by code: {}", tnVedCode);
        ProductClassificationDto classification = productClassificationService.getByCode(tnVedCode);
        return ResponseEntity.ok(classification);
    }

    @GetMapping("/search")
    @Operation(
            summary = "Поиск по префиксу кода",
            description = "Возвращает список товаров, начинающихся с указанного префикса"
    )
    public ResponseEntity<List<ProductClassificationDto>> searchByPrefix(
            @Parameter(description = "Префикс кода ТН ВЭД")
            @RequestParam String prefix) {

        log.info("🔍 Searching product classifications by prefix: {}", prefix);
        List<ProductClassificationDto> classifications = productClassificationService.searchByPrefix(prefix);
        return ResponseEntity.ok(classifications);
    }

    @GetMapping("/search/description")
    @Operation(
            summary = "Поиск по описанию",
            description = "Полнотекстовый поиск товаров по описанию"
    )
    public ResponseEntity<List<ProductClassificationDto>> searchByDescription(
            @Parameter(description = "Поисковый запрос")
            @RequestParam String query) {

        log.info("🔍 Searching product classifications by description: {}", query);
        List<ProductClassificationDto> classifications = productClassificationService.searchByDescription(query);
        return ResponseEntity.ok(classifications);
    }

    @GetMapping("/children/{parentCode}")
    @Operation(
            summary = "Получить дочерние элементы",
            description = "Возвращает список подкатегорий для указанного кода"
    )
    public ResponseEntity<List<ProductClassificationDto>> getChildren(
            @Parameter(description = "Код родительской категории")
            @PathVariable String parentCode) {

        log.info("📤 Fetching child classifications for parent: {}", parentCode);
        List<ProductClassificationDto> children = productClassificationService.getChildren(parentCode);
        return ResponseEntity.ok(children);
    }

    @GetMapping("/root")
    @Operation(
            summary = "Получить корневые элементы",
            description = "Возвращает список всех глав ТН ВЭД (2 знака)"
    )
    public ResponseEntity<List<ProductClassificationDto>> getRootClassifications() {

        log.info("📤 Fetching root classifications");
        List<ProductClassificationDto> roots = productClassificationService.getRootClassifications();
        return ResponseEntity.ok(roots);
    }
}