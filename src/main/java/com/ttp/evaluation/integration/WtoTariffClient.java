package com.ttp.evaluation.integration;

import com.ttp.evaluation.integration.common.RateLimiter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * Клиент для работы с WTO Tariff Schedule API
 * https://goods-schedules.wto.org/member/russian-federation
 * https://api.wto.org/tariff/v1
 *
 * Предоставляет информацию о связанных и применяемых тарифах
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WtoTariffClient {

    private final RestTemplate restTemplate;
    private final RateLimiter rateLimiter;

    @Value("${integration.wto.api-url:https://api.wto.org/tariff/v1}")
    private String apiUrl;

    @Value("${integration.wto.api-key}")
    private String apiKey;

    /**
     * Получение информации о тарифах для товара
     *
     * @param hsCode Код HS (6 цифр)
     * @param memberCode Код члена ВТО (643 для России)
     */
    @CircuitBreaker(name = "external-api", fallbackMethod = "getTariffInfoFallback")
    @Retry(name = "external-api")
    @Cacheable(value = "wto-tariff", key = "#hsCode + '_' + #memberCode", unless = "#result == null")
    public WtoTariffInfo getTariffInfo(String hsCode, String memberCode) {
        log.info("Fetching tariff info from WTO: HS={}, Member={}", hsCode, memberCode);

        rateLimiter.acquire("wto");

        try {
            String url = UriComponentsBuilder.fromHttpUrl(apiUrl + "/schedules")
                    .queryParam("reporter", memberCode)
                    .queryParam("product", hsCode)
                    .queryParam("format", "json")
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", apiKey);

            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<WtoTariffInfo> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    WtoTariffInfo.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.debug("Successfully fetched tariff info from WTO for HS: {}", hsCode);
                return response.getBody();
            }

            return createDefaultTariffInfo(hsCode);

        } catch (Exception e) {
            log.error("Error fetching tariff info from WTO", e);
            return createDefaultTariffInfo(hsCode);
        }
    }

    /**
     * Проверка наличия связывания тарифа
     */
    public boolean checkTariffBinding(String hsCode) {
        WtoTariffInfo info = getTariffInfo(hsCode, "643");
        boolean hasBinding = info != null && info.getBoundRate() != null && info.getBoundRate() > 0;
        log.info("Tariff binding check for HS {}: {}", hsCode, hasBinding);
        return hasBinding;
    }

    /**
     * Получение связанной ставки тарифа
     */
    public Double getBoundTariffRate(String hsCode) {
        WtoTariffInfo info = getTariffInfo(hsCode, "643");
        Double rate = info != null ? info.getBoundRate() : null;
        log.info("Bound tariff rate for HS {}: {}", hsCode, rate);
        return rate;
    }

    /**
     * Получение применяемой ставки тарифа
     */
    public Double getAppliedTariffRate(String hsCode) {
        WtoTariffInfo info = getTariffInfo(hsCode, "643");
        Double rate = info != null ? info.getAppliedRate() : null;
        log.info("Applied tariff rate for HS {}: {}", hsCode, rate);
        return rate;
    }

    /**
     * Проверка возможности повышения тарифа
     */
    public boolean canIncreaseTariff(String hsCode, double targetRate) {
        WtoTariffInfo info = getTariffInfo(hsCode, "643");

        if (info != null && info.getBoundRate() != null && info.getAppliedRate() != null) {
            boolean canIncrease = targetRate <= info.getBoundRate() && targetRate > info.getAppliedRate();
            log.info("Can increase tariff for HS {} to {}%: {}", hsCode, targetRate, canIncrease);
            return canIncrease;
        }

        return false;
    }

    // Fallback method

    private WtoTariffInfo getTariffInfoFallback(String hsCode, String memberCode, Exception e) {
        log.error("Circuit breaker activated for WTO tariff info: {}", e.getMessage());
        return createDefaultTariffInfo(hsCode);
    }

    private WtoTariffInfo createDefaultTariffInfo(String hsCode) {
        return WtoTariffInfo.builder()
                .hsCode(hsCode)
                .boundRate(10.0)      // Значения по умолчанию
                .appliedRate(5.0)
                .tariffType("Ad valorem")
                .hasTariffQuota(false)
                .build();
    }
}

// DTOs для WTO

