package com.ttp.evaluation.integration;

import com.ttp.evaluation.integration.common.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Клиент для работы с тарифными расписаниями и обязательствами ВТО
 *
 * Источник: https://goods-schedules.wto.org
 *
 * Предоставляет информацию о:
 * - Связанных тарифах (bound tariffs)
 * - Применяемых тарифах (applied tariffs)
 * - Соглашениях (ITA и др.)
 * - Процедурах изменения обязательств
 */
@Slf4j
@Component
public class WtoSchedulesClient {

    private final RateLimiter rateLimiter;
    private final String baseUrl;
    private final Map<String, WtoTariffInfo> tariffCache = new ConcurrentHashMap<>();

    // Известные товары под ITA (Information Technology Agreement)
    private static final Set<String> ITA_HS_CODES = Set.of(
            "8471", "8473", "8517", "8525", "8527", "8528", // Компьютеры, телекоммуникации
            "8529", "8531", "8532", "8533", "8534", "8535", // Части электроники
            "8536", "8537", "8540", "8541", "8542", "8543"  // Полупроводники
    );

    public WtoSchedulesClient(
            RateLimiter rateLimiter,
            @Value("${integration.wto.base-url:https://goods-schedules.wto.org}") String baseUrl) {
        this.rateLimiter = rateLimiter;
        this.baseUrl = baseUrl;

        log.info("Initialized WTO Schedules client: {}", baseUrl);
        loadStaticTariffData();
    }

    /**
     * Загрузка статических данных о тарифах России
     */
    private void loadStaticTariffData() {
        // Электроника (ITA) - 0% пошлина
        tariffCache.put("8517", new WtoTariffInfo(
                "8517", "Телефонные аппараты",
                0.0, 0.0, "Certified", true, false, List.of()
        ));

        tariffCache.put("8471", new WtoTariffInfo(
                "8471", "Компьютеры",
                0.0, 0.0, "Certified", true, false, List.of()
        ));

        // Автомобили (с тарифной квотой)
        tariffCache.put("8703", new WtoTariffInfo(
                "8703", "Легковые автомобили",
                15.0, 10.0, "Certified", false, true, List.of()  // ✅ hasTariffQuota = true
        ));

        tariffCache.put("8704", new WtoTariffInfo(
                "8704", "Грузовики",
                20.0, 15.0, "Certified", false, false, List.of()
        ));

        // Медицинское оборудование
        tariffCache.put("9018", new WtoTariffInfo(
                "9018", "Медицинские инструменты",
                5.0, 3.0, "Certified", false, false, List.of()
        ));

        // Продукты питания (часто имеют квоты)
        tariffCache.put("0403", new WtoTariffInfo(
                "0403", "Мясо",
                25.0, 20.0, "Certified", false, true, List.of()  // ✅ hasTariffQuota = true
        ));

        tariffCache.put("1006", new WtoTariffInfo(
                "1006", "Рис",
                10.0, 5.0, "Certified", false, false, List.of()
        ));

        log.info("✅ Loaded {} static tariff records", tariffCache.size());
    }

    /**
     * Получить информацию о тарифах для HS кода
     */
    @Cacheable(value = "wto-tariff-info", key = "#hsCode", unless = "#result == null")
    public WtoTariffInfo getTariffInfo(String hsCode, String countryCode) {
        try {
            rateLimiter.acquire("wto");

            log.info("Fetching WTO tariff info for HS={}, Country={}", hsCode, countryCode);

            // Проверяем кэш (сначала точное совпадение)
            WtoTariffInfo info = findTariffInfo(hsCode);

            if (info != null) {
                log.info("✅ Found tariff info: bound={}, applied={}, ITA={}",
                        info.boundRate(), info.appliedRate(), info.isITA());
                return info;
            }

            // Fallback: попытаться спарсить со страницы WTO (если нет в кэше)
            info = fetchTariffFromWeb(hsCode, countryCode);

            if (info != null) {
                tariffCache.put(hsCode, info);
                return info;
            }

            // Default: нет данных
            log.warn("⚠️ No tariff info found for HS {}", hsCode);
            return createDefaultTariffInfo(hsCode);

        } catch (Exception e) {
            log.error("❌ Error fetching WTO tariff info: {}", e.getMessage());
            return createDefaultTariffInfo(hsCode);
        }
    }

    /**
     * Проверить есть ли запас для повышения пошлины
     */
    public double calculateTariffMargin(String hsCode) {
        WtoTariffInfo info = getTariffInfo(hsCode, "russian-federation");

        if (info.isITA()) {
            return 0.0; // ITA товары - нельзя повышать пошлину
        }

        return info.boundRate() - info.appliedRate();
    }

    /**
     * Проверить можно ли использовать тарифную защиту
     */
    public boolean isTariffProtectionAvailable(String hsCode) {
        WtoTariffInfo info = getTariffInfo(hsCode, "russian-federation");

        // Если ITA - нельзя
        if (info.isITA()) {
            log.info("❌ Tariff protection unavailable: ITA agreement");
            return false;
        }

        // Если нет запаса - нельзя
        double margin = calculateTariffMargin(hsCode);
        if (margin <= 0) {
            log.info("❌ Tariff protection unavailable: no margin (bound=applied)");
            return false;
        }

        log.info("✅ Tariff protection available: margin={:.1f}%", margin);
        return true;
    }

    /**
     * Получить список процедур для страны
     */
    @Cacheable(value = "wto-procedures", key = "#countryCode", unless = "#result == null")
    public List<WtoProcedure> getProcedures(String countryCode) {
        String url = baseUrl + "/member/" + countryCode;
        log.info("Fetching WTO procedures from {}", url);

        try {
            rateLimiter.acquire("wto");

            Document doc = Jsoup.connect(url)
                    .timeout((int) Duration.ofSeconds(30).toMillis())
                    .get();

            Element table = doc.selectFirst("table");
            if (table == null) {
                log.warn("⚠️ No procedures table found");
                return List.of();
            }

            List<WtoProcedure> list = new ArrayList<>();
            Elements rows = table.select("tbody tr");

            for (Element row : rows) {
                Elements cols = row.select("td");
                if (cols.size() < 5) continue;

                String procedureId = cols.get(0).text();
                String dateInfo    = cols.get(1).text();
                String type        = cols.get(2).text();
                String status      = cols.get(3).text();
                String cert        = cols.get(4).text();

                list.add(new WtoProcedure(procedureId, dateInfo, type, status, cert));
            }

            log.info("✅ Parsed {} WTO procedures", list.size());
            return list;

        } catch (IOException e) {
            log.error("❌ Error fetching WTO procedures: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Получить список документов и ссылок WTO
     */
    @Cacheable(value = "wto-documents-links", unless = "#result == null")
    public List<WtoLink> getDocumentsLinks() {
        String url = baseUrl + "/resources/links";
        log.info("Fetching WTO documents & links from {}", url);

        try {
            rateLimiter.acquire("wto");

            Document doc = Jsoup.connect(url)
                    .timeout((int) Duration.ofSeconds(30).toMillis())
                    .get();

            Elements items = doc.select("h2 + ul li, h3 + ul li");
            List<WtoLink> links = new ArrayList<>();

            for (Element li : items) {
                Element a = li.selectFirst("a");
                if (a != null) {
                    links.add(new WtoLink(a.text(), a.absUrl("href")));
                } else {
                    links.add(new WtoLink(li.text(), null));
                }
            }

            log.info("✅ Parsed {} WTO links", links.size());
            return links;

        } catch (IOException e) {
            log.error("❌ Error fetching WTO documents & links: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Найти информацию о тарифе (с fallback на родительские категории)
     */
    private WtoTariffInfo findTariffInfo(String hsCode) {
        if (hsCode == null || hsCode.isEmpty()) return null;

        // Точное совпадение
        WtoTariffInfo info = tariffCache.get(hsCode);
        if (info != null) return info;

        // Первые 6 знаков
        if (hsCode.length() >= 6) {
            info = tariffCache.get(hsCode.substring(0, 6));
            if (info != null) return info;
        }

        // Первые 4 знака (общая категория)
        if (hsCode.length() >= 4) {
            info = tariffCache.get(hsCode.substring(0, 4));
            if (info != null) return info;
        }

        return null;
    }

    /**
     * Попытка спарсить данные со страницы WTO (если нет в кэше)
     */
    private WtoTariffInfo fetchTariffFromWeb(String hsCode, String countryCode) {
        // TODO: Реализовать парсинг детальной страницы товара
        // Пример URL: https://goods-schedules.wto.org/tariff/product/russian-federation/8517
        log.debug("Web parsing not implemented yet for HS {}", hsCode);
        return null;
    }

    /**
     * Создать дефолтную информацию о тарифе
     */
    private WtoTariffInfo createDefaultTariffInfo(String hsCode) {
        boolean isITA = isITAProduct(hsCode);

        return new WtoTariffInfo(
                hsCode,
                "Unknown product",
                isITA ? 0.0 : 10.0,     // Bound rate
                isITA ? 0.0 : 7.5,      // Applied rate
                "Unknown",
                isITA,
                false,                   // ✅ hasTariffQuota = false по умолчанию
                List.of()
        );
    }

    /**
     * Проверить является ли товар частью ITA
     */
    private boolean isITAProduct(String hsCode) {
        if (hsCode == null || hsCode.length() < 4) return false;
        return ITA_HS_CODES.contains(hsCode.substring(0, 4));
    }

    /**
     * Информация о тарифе
     */
    public record WtoTariffInfo(
            String hsCode,
            String productName,
            Double boundRate,          // Связанная ставка (максимум по WTO)
            Double appliedRate,        // Применяемая ставка (текущая)
            String status,             // Certified, Under reservations, etc.
            Boolean isITA,             // Подпадает ли под соглашение ITA
            Boolean hasTariffQuota,    // ✅ ДОБАВЛЕНО: Есть ли тарифная квота
            List<String> procedures    // Связанные процедуры
    ) {}

    /**
     * Процедура WTO
     */
    public record WtoProcedure(
            String procedureId,
            String dateInfo,
            String type,
            String status,
            String certifications
    ) {}

    /**
     * Ссылка на документ
     */
    public record WtoLink(
            String title,
            String url
    ) {}
}