package com.ttp.evaluation.integration;

import com.ttp.evaluation.integration.common.RateLimiter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Клиент для UN Comtrade Public API v1
 * Документация: https://comtradedeveloper.un.org/api-details#api=preview-v1
 */
@Slf4j
@Component
public class UncomtradeClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final RateLimiter rateLimiter;
    private final int defaultHistoryYears;

    // Список "недружественных" стран согласно указу Президента РФ от 05.03.2022 № 95
    private static final Set<String> UNFRIENDLY_COUNTRIES = Set.of(
            "USA", "US", "GBR", "GB", "DEU", "DE", "FRA", "FR", "JPN", "JP",
            "CAN", "CA", "AUS", "AU", "NZL", "NZ", "KOR", "KR", "SGP", "SG",
            "NOR", "NO", "CHE", "CH", "ISL", "IS", "AND", "AD", "ALB", "AL",
            "MNE", "ME", "MKD", "MK", "LIE", "LI", "SMR", "SM", "MCO", "MC"
    );

    public UncomtradeClient(
            RestTemplate restTemplate,
            RateLimiter rateLimiter,
            @Value("${integration.uncomtrade.base-url}") String baseUrl,
            @Value("${integration.uncomtrade.history-years:3}") int defaultHistoryYears) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.rateLimiter = rateLimiter;
        this.defaultHistoryYears = defaultHistoryYears;
    }

    /**
     * Получить торговые данные по коду HS
     */
    @Cacheable(value = "uncomtrade-data", key = "#hsCode + '-' + #reporterISO + '-' + #year", unless = "#result == null")
    @CircuitBreaker(name = "uncomtrade", fallbackMethod = "getTradeDataFallback")
    public List<UncomtradeResponse.TradeData> getTradeData(String hsCode, String reporterISO, Integer year) {
        try {
            rateLimiter.acquire("uncomtrade");

            log.info("Fetching trade data from UN Comtrade: HS={}, Reporter={}, Year={}",
                    hsCode, reporterISO, year);

            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/preview/C/A/HS")
                    .queryParam("cmdCode", hsCode)
                    .queryParam("partnerCode", "0")
                    .queryParam("period", year)
                    .queryParam("reporterCode", getCountryCode(reporterISO))
                    .build()
                    .toUriString();

            log.debug("UN Comtrade API URL: {}", url);

            UncomtradeResponse response = restTemplate.getForObject(url, UncomtradeResponse.class);

            if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                log.info("✅ Successfully fetched {} trade records from UN Comtrade", response.getData().size());
                return response.getData();
            }

            log.warn("⚠️ No trade data found for HS={}, Reporter={}, Year={}", hsCode, reporterISO, year);
            return Collections.emptyList();

        } catch (HttpClientErrorException.TooManyRequests e) {
            log.error("❌ 429 from UN Comtrade - STOPPING retry, returning empty");
            return Collections.emptyList(); // ← НЕ бросать исключение, вернуть пустой список

        } catch (Exception e) {
            log.error("❌ Error fetching trade data from UN Comtrade: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Получить историю импорта России за последние N лет
     */
    public List<ImportHistory> getRussianImportHistory(String hsCode, int years) {
        int currentYear = LocalDate.now().getYear();
        List<ImportHistory> history = new ArrayList<>();

        for (int i = 0; i < years; i++) {
            int year = currentYear - i;
            try {
                rateLimiter.acquire("uncomtrade");
                List<UncomtradeResponse.TradeData> data = getTradeData(hsCode, "RUS", year);

                double totalImport = data.stream()
                        .filter(trade -> "M".equals(trade.getFlowCode()))
                        .mapToDouble(trade -> trade.getPrimaryValue() != null ? trade.getPrimaryValue() : 0)
                        .sum();

                double totalQuantity = data.stream()
                        .filter(trade -> "M".equals(trade.getFlowCode()))
                        .mapToDouble(trade -> trade.getQty() != null ? trade.getQty() : 0)
                        .sum();

                history.add(new ImportHistory(year, totalImport, totalQuantity));

            } catch (Exception e) {
                log.warn("Failed to fetch import history for year {}: {}", year, e.getMessage());
            }
        }

        return history;
    }

    /**
     * Получить историю импорта России в формате UnComtradeRecord
     * Для совместимости со старым кодом
     */
    public List<UnComtradeRecord> getRussianImportHistoryAsRecords(String hsCode) {
        List<ImportHistory> history = getRussianImportHistory(hsCode, defaultHistoryYears);

        return history.stream()
                .map(h -> toUnComtradeRecord(h, hsCode))
                .collect(Collectors.toList());
    }

    /**
     * Рассчитать долю импорта из "недружественных" стран
     */
    public double calculateUnfriendlyCountriesShare(String hsCode, Integer year) {
        try {
            List<UncomtradeResponse.TradeData> data = getTradeData(hsCode, "RUS", year);

            double totalImport = 0;
            double unfriendlyImport = 0;

            for (UncomtradeResponse.TradeData trade : data) {
                if ("M".equals(trade.getFlowCode())) {
                    double value = trade.getPrimaryValue() != null ? trade.getPrimaryValue() : 0;
                    totalImport += value;

                    if (isUnfriendlyCountry(trade.getPartnerISO())) {
                        unfriendlyImport += value;
                    }
                }
            }

            if (totalImport == 0) {
                return 0.0;
            }

            double share = (unfriendlyImport / totalImport) * 100;
            log.info("Unfriendly countries share for HS {}: {:.2f}%", hsCode, share);

            return share;

        } catch (Exception e) {
            log.error("Error calculating unfriendly countries share: {}", e.getMessage());
            return 0.0;
        }
    }

    /**
     * Получить агрегированные данные импорта/экспорта
     */
    public TradeMetrics getTradeMetrics(String hsCode, String reporterISO, Integer year) {
        List<UncomtradeResponse.TradeData> data = getTradeData(hsCode, reporterISO, year);

        double totalImportValue = 0;
        double totalExportValue = 0;
        double totalImportQty = 0;
        double totalExportQty = 0;

        for (UncomtradeResponse.TradeData trade : data) {
            if ("M".equals(trade.getFlowCode())) {
                totalImportValue += trade.getPrimaryValue() != null ? trade.getPrimaryValue() : 0;
                totalImportQty += trade.getQty() != null ? trade.getQty() : 0;
            } else if ("X".equals(trade.getFlowCode())) {
                totalExportValue += trade.getPrimaryValue() != null ? trade.getPrimaryValue() : 0;
                totalExportQty += trade.getQty() != null ? trade.getQty() : 0;
            }
        }

        log.info("Trade metrics for HS={}: Import=${}, Export=${}",
                hsCode, totalImportValue, totalExportValue);

        return new TradeMetrics(
                hsCode,
                reporterISO,
                year,
                totalImportValue,
                totalImportQty,
                totalExportValue,
                totalExportQty
        );
    }

    /**
     * Конвертация ImportHistory в UnComtradeRecord для совместимости
     */
    private static UnComtradeRecord toUnComtradeRecord(ImportHistory history, String hsCode) {
        return UnComtradeRecord.builder()
                .reporterCode("643")
                .reporterDesc("Russian Federation")
                .partnerCode("0")
                .partnerDesc("World")
                .cmdCode(hsCode)
                .cmdDesc("")
                .flowCode("M")
                .flowDesc("Import")
                .period(history.year()) // ✅ period = year
                .primaryValue(history.importValue())
                .netWeight(0.0)
                .qty(history.quantity())
                .qtyUnitCode("")
                .build();
    }

    /**
     * Проверка является ли страна "недружественной"
     */
    private boolean isUnfriendlyCountry(String iso) {
        if (iso == null) return false;
        return UNFRIENDLY_COUNTRIES.contains(iso.toUpperCase());
    }

    /**
     * Конвертация ISO кода страны в UN Comtrade код
     */
    private Integer getCountryCode(String iso) {
        if (iso == null || iso.isEmpty()) {
            return 0;
        }

        return switch (iso.toUpperCase()) {
            case "RUS", "RU" -> 643;
            case "CHN", "CN" -> 156;
            case "USA", "US" -> 842;
            case "DEU", "DE" -> 276;
            case "JPN", "JP" -> 392;
            case "GBR", "GB" -> 826;
            case "FRA", "FR" -> 250;
            case "IND", "IN" -> 356;
            case "BRA", "BR" -> 76;
            case "KOR", "KR" -> 410;
            default -> {
                log.warn("Unknown country ISO code: {}, using World (0)", iso);
                yield 0;
            }
        };
    }

    /**
     * История импорта
     */
    public record ImportHistory(
            Integer year,
            Double importValue,
            Double quantity
    ) {}

    /**
     * Агрегированные торговые метрики
     */
    public record TradeMetrics(
            String hsCode,
            String reporterISO,
            Integer year,
            Double totalImportValue,
            Double totalImportQty,
            Double totalExportValue,
            Double totalExportQty
    ) {}
}