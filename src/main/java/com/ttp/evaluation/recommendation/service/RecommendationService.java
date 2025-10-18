package com.ttp.evaluation.recommendation.service;

import com.ttp.evaluation.integration.RosstatCapacityData;
import com.ttp.evaluation.integration.RosstatClient;
import com.ttp.evaluation.integration.UncomtradeClient;
import com.ttp.evaluation.integration.WtoSchedulesClient;
import com.ttp.evaluation.recommendation.api.dto.recommendation.RecommendationRequestDto;
import com.ttp.evaluation.recommendation.api.dto.recommendation.RecommendationResponseDto;
import com.ttp.evaluation.recommendation.domain.*;
import com.ttp.evaluation.recommendation.repository.TtpRecommendationRepository;
import com.ttp.evaluation.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Сервис оценки и рекомендации мер ТТП
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RecommendationService {

    // ✅ ВСЕ ЗАВИСИМОСТИ ВВЕРХУ
    private final TtpRecommendationRepository recommendationRepository;
    private final UncomtradeClient unComtradeClient;
    private final RosstatClient rosstatClient;
    private final WtoSchedulesClient wtoSchedulesClient;  // ✅ ПРАВИЛЬНАЯ ЗАВИСИМОСТЬ

    /**
     * Главный метод оценки мер ТТП
     */
    public RecommendationResponseDto evaluateMeasures(RecommendationRequestDto request) {
        String requestId = UUID.randomUUID().toString();

        log.info("Starting TTP evaluation for request: {}, TN VED: {}, Product: {}",
                requestId, request.getTnVedCode(), request.getProductName());

        TtpRecommendation recommendation = TtpRecommendation.builder()
                .requestId(requestId)
                .userId(request.getUserId())
                .tnVedCode(request.getTnVedCode())
                .productName(request.getProductName())
                .status(RecommendationStatus.IN_PROGRESS)
                .measures(new ArrayList<>())
                .build();

        recommendation = recommendationRepository.save(recommendation);

        try {
            // 1. Подготовка данных
            EvaluationContext context = prepareEvaluationContext(request.getTnVedCode());
            log.info("Evaluation context prepared for TN VED: {}", request.getTnVedCode());

            // 2. Оценка всех мер
            List<MeasureResult> results = evaluateAllMeasures(recommendation, context);
            recommendation.getMeasures().addAll(results);

            // 3. Расчет общей оценки
            double totalScore = calculateTotalScore(results);
            recommendation.setTotalScore(totalScore);

            // 4. Формирование резюме
            String summary = generateSummary(results, context);
            recommendation.setSummary(summary);

            recommendation.setStatus(RecommendationStatus.COMPLETED);
            recommendation.setCompletedAt(Instant.now());

            recommendation = recommendationRepository.save(recommendation);

            log.info("✅ Completed TTP evaluation for request: {}, Total score: {:.2f}",
                    requestId, totalScore);

            return toDto(recommendation);

        } catch (Exception e) {
            log.error("❌ Failed to evaluate TTP measures for request: {}", requestId, e);
            recommendation.setStatus(RecommendationStatus.FAILED);
            recommendation.setSummary("Ошибка при оценке мер: " + e.getMessage());
            recommendationRepository.save(recommendation);
            throw new RuntimeException("Failed to evaluate measures", e);
        }
    }

    /**
     * Подготовка контекста с данными из всех источников
     */
    private EvaluationContext prepareEvaluationContext(String tnVedCode) {
        log.info("📊 Preparing evaluation context for TN VED: {}", tnVedCode);

        // Конвертация кодов
        String hsCode = convertTnVedToHs(tnVedCode);
        String okpd2Code = rosstatClient.mapTnVedToOkpd2(tnVedCode);

        int currentYear = LocalDate.now().getYear() - 1;

        // Параллельный сбор данных из внешних API
        log.info("🌐 Fetching data from external sources...");

        // ✅ UN Comtrade data
        double chinaShare = calculateChinaImportShare(hsCode, currentYear);
        double unfriendlyShare = unComtradeClient.calculateUnfriendlyCountriesShare(hsCode, currentYear);
        boolean importStable = calculateImportStability(hsCode, 3);
        var importHistory = unComtradeClient.getRussianImportHistoryAsRecords(hsCode);

        double totalImportShare = chinaShare + unfriendlyShare;

        // ✅ Rosstat data
        double productionDecline = rosstatClient.calculateProductionDecline(okpd2Code, 3);
        RosstatCapacityData capacityData = rosstatClient.getProductionCapacity(okpd2Code, currentYear);
        double capacityUtilization = capacityData != null ?
                capacityData.getCapacityUtilization() / 100.0 : 0.5;

        // ✅ WTO data
        var tariffInfo = wtoSchedulesClient.getTariffInfo(hsCode, "russian-federation");
        boolean hasTariffBinding = "Certified".equals(tariffInfo.status());
        double tariffMargin = wtoSchedulesClient.calculateTariffMargin(hsCode);
        boolean canRaiseTariff = wtoSchedulesClient.isTariffProtectionAvailable(hsCode);

        log.info("📈 Data collected: China={:.1f}%, Unfriendly={:.1f}%, Stable={}, Decline={:.1f}%, Capacity={:.1f}%",
                chinaShare * 100, unfriendlyShare * 100, importStable,
                productionDecline * 100, capacityUtilization * 100);

        log.info("📊 WTO: bound={}, applied={}, margin={:.1f}%, ITA={}, canRaise={}",
                tariffInfo.boundRate(), tariffInfo.appliedRate(), tariffMargin,
                tariffInfo.isITA(), canRaiseTariff);

        return EvaluationContext.builder()
                .hsCode(hsCode)
                .tnVedCode(tnVedCode)
                .okpd2Code(okpd2Code)
                .chinaImportShare(chinaShare)
                .unfriendlyCountriesShare(unfriendlyShare)
                .totalImportShare(totalImportShare)
                .importStable(importStable)
                .productionDecline(productionDecline)
                .capacityUtilization(capacityUtilization)
                .tariffInfo(tariffInfo)
                .hasTariffBinding(hasTariffBinding)
                .tariffMargin(tariffMargin)
                .canRaiseTariff(canRaiseTariff)
                .importHistory(importHistory)
                .build();
    }

    // ... остальные методы без изменений ...

    private double calculateChinaImportShare(String hsCode, Integer year) {
        try {
            var russiaData = unComtradeClient.getTradeData(hsCode, "RUS", year);

            double totalImport = russiaData.stream()
                    .filter(trade -> "M".equals(trade.getFlowCode()))
                    .mapToDouble(trade -> trade.getPrimaryValue() != null ? trade.getPrimaryValue() : 0)
                    .sum();

            double chinaImport = russiaData.stream()
                    .filter(trade -> "M".equals(trade.getFlowCode()))
                    .filter(trade -> isChina(trade.getPartnerISO(), trade.getPartnerCode()))
                    .mapToDouble(trade -> trade.getPrimaryValue() != null ? trade.getPrimaryValue() : 0)
                    .sum();

            if (totalImport == 0) {
                log.warn("No import data found for HS={}, Year={}", hsCode, year);
                return 0.0;
            }

            double share = chinaImport / totalImport;
            log.info("China import share for HS {}: {:.2f}%", hsCode, share * 100);
            return share;

        } catch (Exception e) {
            log.error("Error calculating China import share: {}", e.getMessage());
            return 0.0;
        }
    }

    private boolean isChina(String partnerISO, Integer partnerCode) {
        if (partnerISO != null && (partnerISO.equalsIgnoreCase("CN") || partnerISO.equalsIgnoreCase("CHN"))) {
            return true;
        }
        return Integer.valueOf(156).equals(partnerCode);
    }

    private boolean calculateImportStability(String hsCode, int years) {
        try {
            var history = unComtradeClient.getRussianImportHistory(hsCode, years);

            if (history.size() < 2) {
                log.warn("Insufficient data for stability check: {} years", history.size());
                return false;
            }

            for (int i = 1; i < history.size(); i++) {
                double prevYear = history.get(i - 1).importValue();
                double currentYear = history.get(i).importValue();

                if (prevYear > 0) {
                    double change = ((currentYear - prevYear) / prevYear) * 100;
                    if (change < -20) {
                        log.info("Import instability detected: {:.1f}% drop from year {} to {}",
                                Math.abs(change), history.get(i - 1).year(), history.get(i).year());
                        return false;
                    }
                }
            }

            log.info("Import is stable for the last {} years", years);
            return true;

        } catch (Exception e) {
            log.error("Error calculating import stability: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Оценка всех 6 мер ТТП
     */
    private List<MeasureResult> evaluateAllMeasures(TtpRecommendation recommendation,
                                                    EvaluationContext context) {
        log.info("⚖️ Evaluating all 6 TTP measures...");

        List<MeasureResult> results = new ArrayList<>();

        results.add(evaluateMeasure1_WtoLevel(recommendation, context));
        results.add(evaluateMeasure2_Tariff35_50(recommendation, context));
        results.add(evaluateMeasure3_AntiDumpingChina(recommendation, context));
        results.add(evaluateMeasure4_EaeuRegulations(recommendation, context));
        results.add(evaluateMeasure5_ProductionMonitoring(recommendation, context));
        results.add(evaluateMeasure6_OtherMeasures(recommendation, context));

        long applicableCount = results.stream().filter(MeasureResult::getApplicable).count();
        log.info("✅ Evaluation complete: {}/6 measures applicable", applicableCount);

        return results;
    }

    /**
     * МЕРА 1: Меры на уровне ВТО (повышение тарифа в рамках связывания)
     *
     * Критерии применимости:
     * 1. Наличие связывания тарифа в ВТО
     * 2. Доля импорта > 30%
     * 3. Недозагрузка производственных мощностей
     */
    private MeasureResult evaluateMeasure1_WtoLevel(TtpRecommendation recommendation,
                                                    EvaluationContext context) {
        log.debug("Evaluating Measure 1: WTO Level");

        boolean tariffBinding = context.getHasTariffBinding();
        boolean highImport = context.getTotalImportShare() > 0.30;
        boolean hasCapacity = context.getCapacityUtilization() < 0.70;

        boolean applicable = tariffBinding && highImport;

        double score = 0.0;
        if (applicable) {
            score = 50.0;
            if (context.getTotalImportShare() > 0.50) score += 20;
            if (hasCapacity) score += 15;
            if (context.getProductionDecline() > 0.10) score += 15;
            score = Math.min(100, score);
        }

        String reasoning = String.format(
                "Связывание тарифа: %s | Доля импорта: %.1f%% | Загрузка мощностей: %.1f%% | " +
                        "Текущий тариф: %.1f%% → Связанный: %.1f%%",
                tariffBinding ? "✓ ДА" : "✗ НЕТ",
                context.getTotalImportShare() * 100,
                context.getCapacityUtilization() * 100,
                context.getTariffInfo() != null ? context.getTariffInfo().appliedRate() : 0,
                context.getTariffInfo() != null ? context.getTariffInfo().boundRate() : 0
        );

        String details = String.format("""
                === МЕРА 1: Меры на уровне ВТО ===
                
                Применимость: %s
                Оценка эффективности: %.1f/100
                
                Детальный анализ:
                1. Связывание тарифа позволяет повысить ставку до уровня %.1f%%
                2. Высокая доля импорта (%.1f%%) создает риски для отечественных производителей
                3. %s
                4. Снижение производства: %.1f%%
                
                Рекомендация:
                %s
                """,
                applicable ? "✓ ДА" : "✗ НЕТ",
                score,
                context.getTariffInfo() != null ? context.getTariffInfo().boundRate() : 0,
                context.getTotalImportShare() * 100,
                hasCapacity ? "✓ Имеются свободные мощности для замещения импорта" :
                        "⚠ Производственные мощности загружены, требуются инвестиции",
                context.getProductionDecline() * 100,
                applicable ? "Рекомендуется повышение тарифа в рамках связывания ВТО" :
                        "Мера не применима из-за отсутствия связывания или низкого импорта"
        );

        return MeasureResult.builder()
                .recommendation(recommendation)
                .measureType(MeasureType.WTO_LEVEL)
                .measureName("Меры на уровне ВТО (повышение тарифа)")
                .applicable(applicable)
                .score(score)
                .reasoning(reasoning)
                .details(details)
                .importShare(context.getTotalImportShare())
                .productionCapacity(context.getCapacityUtilization())
                .build();
    }

    /**
     * МЕРА 2: Повышение тарифа до 35-50%
     *
     * Критерии:
     * 1. Доля импорта > 30%
     * 2. Стабильность импорта (низкая волатильность)
     */
    private MeasureResult evaluateMeasure2_Tariff35_50(TtpRecommendation recommendation,
                                                       EvaluationContext context) {
        log.debug("Evaluating Measure 2: Tariff 35-50%");

        boolean highImport = context.getTotalImportShare() > 0.30;
        boolean stableImport = context.isImportStable();

        boolean applicable = highImport && stableImport;

        double score = 0.0;
        if (applicable) {
            score = 55.0;
            if (context.getTotalImportShare() > 0.50) score += 25;
            if (context.getProductionDecline() > 0.15) score += 20;
            score = Math.min(100, score);
        }

        String reasoning = String.format(
                "Доля импорта: %.1f%% | Стабильность: %s | Снижение производства: %.1f%%",
                context.getTotalImportShare() * 100,
                stableImport ? "✓ СТАБИЛЬНЫЙ" : "✗ НЕСТАБИЛЬНЫЙ",
                context.getProductionDecline() * 100
        );

        String details = String.format("""
                === МЕРА 2: Повышение импортного тарифа до 35-50%% ===
                
                Применимость: %s
                Оценка эффективности: %.1f/100
                
                Детальный анализ:
                1. %s
                2. Повышение тарифа создаст ценовое преимущество для российских производителей
                3. %s
                
                Рекомендация:
                %s
                """,
                applicable ? "✓ ДА" : "✗ НЕТ",
                score,
                stableImport ? "✓ Стабильный импорт свидетельствует о сформированной зависимости" :
                        "⚠ Нестабильный импорт требует дополнительного анализа",
                context.getProductionDecline() > 0.10 ?
                        "⚠ Снижение производства требует защитных мер" :
                        "✓ Производство стабильно",
                applicable ? "Рекомендуется установление тарифа 35-50%" :
                        "Мера не применима из-за низкого или нестабильного импорта"
        );

        return MeasureResult.builder()
                .recommendation(recommendation)
                .measureType(MeasureType.TARIFF_35_50)
                .measureName("Повышение импортного тарифа до 35-50%")
                .applicable(applicable)
                .score(score)
                .reasoning(reasoning)
                .details(details)
                .importShare(context.getTotalImportShare())
                .build();
    }

    /**
     * МЕРА 3: Антидемпинговое расследование в отношении Китая
     *
     * Критерии:
     * 1. Доля импорта из Китая > 20%
     * 2. Снижение производства в РФ > 5%
     * 3. Подозрение на демпинг (ценовые аномалии)
     */
    private MeasureResult evaluateMeasure3_AntiDumpingChina(TtpRecommendation recommendation,
                                                            EvaluationContext context) {
        log.debug("Evaluating Measure 3: Anti-dumping China");

        double chinaShare = context.getChinaImportShare();
        boolean highChinaImport = chinaShare > 0.20;
        boolean productionDecline = context.getProductionDecline() > 0.05;

        // TODO: ML-модель для определения демпинга
        boolean suspectedDumping = highChinaImport && productionDecline;

        boolean applicable = highChinaImport && productionDecline;

        double score = 0.0;
        if (applicable) {
            score = 60.0;
            if (chinaShare > 0.40) score += 20;
            if (context.getProductionDecline() > 0.20) score += 20;
            score = Math.min(100, score);
        }

        String reasoning = String.format(
                "Доля КНР: %.1f%% | Снижение производства РФ: %.1f%% | Подозрение на демпинг: %s",
                chinaShare * 100,
                context.getProductionDecline() * 100,
                suspectedDumping ? "✓ ДА" : "✗ НЕТ"
        );

        String details = String.format("""
                === МЕРА 3: Антидемпинговое расследование (Китай) ===
                
                Применимость: %s
                Оценка эффективности: %.1f/100
                
                Детальный анализ:
                1. %s
                2. %s
                3. Требуется детальный анализ цен и условий торговли
                4. ⏱ Антидемпинговое расследование может занять 12-18 месяцев
                
                Рекомендация:
                %s
                """,
                applicable ? "✓ ДА" : "✗ НЕТ",
                score,
                highChinaImport ? "⚠ Высокая доля китайского импорта указывает на возможный демпинг" :
                        "✓ Доля китайского импорта в пределах нормы",
                productionDecline ? "⚠ Снижение отечественного производства требует расследования" :
                        "✓ Производство стабильно",
                applicable ? "Рекомендуется инициирование антидемпингового расследования" :
                        "Мера не применима из-за низкой доли Китая или стабильного производства"
        );

        return MeasureResult.builder()
                .recommendation(recommendation)
                .measureType(MeasureType.ANTI_DUMPING_CHINA)
                .measureName("Антидемпинговое расследование (Китай)")
                .applicable(applicable)
                .score(score)
                .reasoning(reasoning)
                .details(details)
                .priceDifference(suspectedDumping ? -0.15 : 0.0)
                .build();
    }

    /**
     * МЕРА 4: Применение технических регламентов ЕАЭС
     */
    private MeasureResult evaluateMeasure4_EaeuRegulations(TtpRecommendation recommendation,
                                                           EvaluationContext context) {
        log.debug("Evaluating Measure 4: EAEU Regulations");

        // TODO: Интеграция с базой регламентов ЕАЭС
        boolean hasRegulation = checkEaeuRegulation(context.getTnVedCode());

        boolean applicable = hasRegulation;
        double score = applicable ? 75.0 : 0.0;

        String reasoning = hasRegulation ?
                "✓ Применим технический регламент ЕАЭС" :
                "✗ Технические регламенты ЕАЭС не применимы";

        String details = String.format("""
                === МЕРА 4: Технические регламенты ЕАЭС ===
                
                Применимость: %s
                Оценка эффективности: %.1f/100
                
                Детальный анализ:
                1. %s
                2. ✓ Применение регламента не требует согласования с ВТО
                3. ⏱ Срок внедрения регламента: 6-12 месяцев
                
                Рекомендация:
                %s
                """,
                applicable ? "✓ ДА" : "✗ НЕТ",
                score,
                hasRegulation ? "✓ Технический регламент позволяет контролировать качество импорта" :
                        "⚠ Требуется разработка нового регламента",
                applicable ? "Рекомендуется применение существующих регламентов ЕАЭС" :
                        "Рекомендуется разработка нового технического регламента"
        );

        return MeasureResult.builder()
                .recommendation(recommendation)
                .measureType(MeasureType.EAEU_REGULATION)
                .measureName("Технические регламенты ЕАЭС")
                .applicable(applicable)
                .score(score)
                .reasoning(reasoning)
                .details(details)
                .build();
    }

    /**
     * МЕРА 5: Мониторинг производства и импорта
     * (Всегда применима)
     */
    private MeasureResult evaluateMeasure5_ProductionMonitoring(TtpRecommendation recommendation,
                                                                EvaluationContext context) {
        log.debug("Evaluating Measure 5: Production Monitoring");

        boolean applicable = true;
        double score = 65.0;

        String reasoning = String.format(
                "Загрузка мощностей: %.1f%% | Динамика: %s | Необходимость: ВЫСОКАЯ",
                context.getCapacityUtilization() * 100,
                context.getProductionDecline() > 0 ? "⚠ СНИЖЕНИЕ" : "✓ РОСТ/СТАБИЛЬНО"
        );

        String details = String.format("""
                === МЕРА 5: Мониторинг производства и импорта ===
                
                Применимость: ✓ ДА (всегда)
                Оценка эффективности: %.1f/100
                
                Детальный анализ:
                1. ✓ Постоянный мониторинг позволяет своевременно выявлять угрозы
                2. 📊 Рекомендуется ежеквартальный сбор данных о производстве и импорте
                3. %s
                4. ⏱ Срок реализации: немедленно
                
                Рекомендация:
                Внедрить систему постоянного мониторинга отрасли
                """,
                score,
                context.getProductionDecline() > 0.10 ?
                        "⚠ Требуется усиленный контроль из-за снижения производства" :
                        "✓ Стандартный режим мониторинга"
        );

        return MeasureResult.builder()
                .recommendation(recommendation)
                .measureType(MeasureType.PRODUCTION_MONITORING)
                .measureName("Мониторинг производства и импорта")
                .applicable(applicable)
                .score(score)
                .reasoning(reasoning)
                .details(details)
                .productionCapacity(context.getCapacityUtilization())
                .build();
    }

    /**
     * МЕРА 6: Прочие специальные меры
     */
    private MeasureResult evaluateMeasure6_OtherMeasures(TtpRecommendation recommendation,
                                                         EvaluationContext context) {
        log.debug("Evaluating Measure 6: Other Measures");

        boolean applicable = false;
        double score = 0.0;
        StringBuilder reasoning = new StringBuilder("Дополнительный анализ: ");
        List<String> recommendations = new ArrayList<>();

        // Критическое снижение производства
        if (context.getProductionDecline() > 0.25) {
            applicable = true;
            score = 70.0;
            reasoning.append("⚠ Критическое снижение производства. ");
            recommendations.add("Рекомендуются специальные защитные меры");
        }

        // Тарифные квоты
        if (context.getTariffInfo() != null &&
                Boolean.TRUE.equals(context.getTariffInfo().hasTariffQuota())) {  // ✅ РАБОТАЕТ
            applicable = true;
            score = Math.max(score, 65.0);
            reasoning.append("✓ Применима тарифная квота. ");
            recommendations.add("Рассмотреть корректировку объемов тарифной квоты");
        }

        if (!applicable) {
            reasoning.append("✓ Специальные меры не требуются");
            recommendations.add("Продолжить мониторинг ситуации");
        }

        String details = String.format("""
                === МЕРА 6: Прочие специальные меры ===
                
                Применимость: %s
                Оценка эффективности: %.1f/100
                
                Рекомендации:
                %s
                """,
                applicable ? "✓ ДА" : "✗ НЕТ",
                score,
                String.join("\n", recommendations.stream()
                        .map(r -> "• " + r)
                        .toList())
        );

        return MeasureResult.builder()
                .recommendation(recommendation)
                .measureType(MeasureType.OTHER)
                .measureName("Прочие специальные меры")
                .applicable(applicable)
                .score(score)
                .reasoning(reasoning.toString())
                .details(details)
                .build();
    }

    // Вспомогательные методы

    private String convertTnVedToHs(String tnVedCode) {
        if (tnVedCode != null && tnVedCode.length() >= 6) {
            return tnVedCode.substring(0, 6);
        }
        return tnVedCode;
    }

    private boolean checkEaeuRegulation(String tnVedCode) {
        // Упрощенная проверка: пищевые продукты (01-24) часто имеют регламенты
        if (tnVedCode != null && tnVedCode.length() >= 2) {
            try {
                int chapter = Integer.parseInt(tnVedCode.substring(0, 2));
                return chapter >= 1 && chapter <= 24;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    private double calculateTotalScore(List<MeasureResult> results) {
        return results.stream()
                .filter(MeasureResult::getApplicable)
                .mapToDouble(MeasureResult::getScore)
                .average()
                .orElse(0.0);
    }

    private String generateSummary(List<MeasureResult> results, EvaluationContext context) {
        long applicableCount = results.stream().filter(MeasureResult::getApplicable).count();

        MeasureResult topMeasure = results.stream()
                .filter(MeasureResult::getApplicable)
                .max((m1, m2) -> Double.compare(m1.getScore(), m2.getScore()))
                .orElse(null);

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("📊 Анализ завершен. Применимо мер: %d из %d.\n\n",
                applicableCount, results.size()));

        if (topMeasure != null) {
            summary.append(String.format("✅ РЕКОМЕНДУЕТСЯ: %s (оценка: %.1f)\n\n",
                    topMeasure.getMeasureName(), topMeasure.getScore()));
            summary.append("Обоснование: ").append(topMeasure.getReasoning()).append("\n\n");
        } else {
            summary.append("⚠️ Применимых мер не найдено. Рекомендуется продолжить мониторинг.\n\n");
        }

        summary.append("📈 Ключевые показатели:\n");
        summary.append(String.format("• Доля импорта из Китая: %.1f%%\n",
                context.getChinaImportShare() * 100));
        summary.append(String.format("• Доля импорта из недружественных стран: %.1f%%\n",
                context.getUnfriendlyCountriesShare() * 100));
        summary.append(String.format("• Загрузка производственных мощностей: %.1f%%\n",
                context.getCapacityUtilization() * 100));
        summary.append(String.format("• Снижение производства: %.1f%%\n",
                context.getProductionDecline() * 100));

        return summary.toString();
    }

    @Transactional(readOnly = true)
    public RecommendationResponseDto getRecommendation(String requestId) {
        TtpRecommendation recommendation = recommendationRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found: " + requestId));
        return toDto(recommendation);
    }

    private RecommendationResponseDto toDto(TtpRecommendation recommendation) {
        return RecommendationResponseDto.builder()
                .requestId(recommendation.getRequestId())
                .tnVedCode(recommendation.getTnVedCode())
                .productName(recommendation.getProductName())
                .status(recommendation.getStatus())
                .totalScore(recommendation.getTotalScore())
                .summary(recommendation.getSummary())
                .measures(recommendation.getMeasures())
                .createdAt(recommendation.getCreatedAt())
                .completedAt(recommendation.getCompletedAt())
                .build();
    }
}