package com.ttp.evaluation.integration;

import com.ttp.evaluation.integration.common.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Клиент для работы с ЕМИСС (fedstat.ru) через SDMX API
 *
 * SDMX = Statistical Data and Metadata Exchange
 * Международный стандарт обмена статистическими данными
 *
 * Документация: https://fedstat.ru/sdmx
 *
 * Основные индикаторы:
 * - 31074: Индекс промышленного производства
 * - 58036: Использование производственных мощностей
 * - 40616: Объем отгруженной продукции
 */
@Slf4j
@Component
public class EmissClient {

    private final RestTemplate restTemplate;
    private final RateLimiter rateLimiter;
    private final String baseUrl;

    // Маппинг ОКПД2 → ЕМИСС индикаторы
    private static final String INDUSTRIAL_PRODUCTION_INDEX = "31074";
    private static final String CAPACITY_UTILIZATION = "58036";
    private static final String SHIPPED_PRODUCTION = "40616";

    public EmissClient(
            RestTemplate restTemplate,
            RateLimiter rateLimiter,
            @Value("${integration.emiss.base-url:https://fedstat.ru/sdmx/v2}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.rateLimiter = rateLimiter;
        this.baseUrl = baseUrl;

        log.info("Initialized EMISS client with SDMX API: {}", baseUrl);
    }

    /**
     * Получить индекс промышленного производства
     *
     * @param okpd2Code код ОКПД2 (опционально)
     * @param year год
     * @return индекс производства (100 = базовый уровень)
     */
    @Cacheable(value = "emiss-production-index", key = "#okpd2Code + '-' + #year", unless = "#result == null")
    public Double getIndustrialProductionIndex(String okpd2Code, Integer year) {
        try {
            rateLimiter.acquire("emiss");

            log.info("Fetching industrial production index for OKPD2={}, Year={}", okpd2Code, year);

            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/data/" + INDUSTRIAL_PRODUCTION_INDEX)
                    .queryParam("startPeriod", year)
                    .queryParam("endPeriod", year)
                    .queryParam("format", "sdmx-json")
                    .build()
                    .toUriString();

            log.debug("EMISS SDMX URL: {}", url);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Парсим SDMX-JSON или SDMX-XML
                Double index = parseSdmxResponse(response.getBody());

                if (index != null) {
                    log.info("✅ Industrial production index: {}", index);
                    return index;
                }
            }

            log.warn("⚠️ No production index data found");
            return null;

        } catch (Exception e) {
            log.error("❌ Error fetching production index from EMISS: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Получить загрузку производственных мощностей
     *
     * @param okpd2Code код ОКПД2
     * @param year год
     * @return процент загрузки (0-100)
     */
    @Cacheable(value = "emiss-capacity", key = "#okpd2Code + '-' + #year", unless = "#result == null")
    public Double getCapacityUtilization(String okpd2Code, Integer year) {
        try {
            rateLimiter.acquire("emiss");

            log.info("Fetching capacity utilization for OKPD2={}, Year={}", okpd2Code, year);

            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/data/" + CAPACITY_UTILIZATION)
                    .queryParam("startPeriod", year)
                    .queryParam("endPeriod", year)
                    .queryParam("format", "sdmx-json")
                    .build()
                    .toUriString();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Double capacity = parseSdmxResponse(response.getBody());

                if (capacity != null) {
                    log.info("✅ Capacity utilization: {}%", capacity);
                    return capacity;
                }
            }

            log.warn("⚠️ No capacity data found");
            return null;

        } catch (Exception e) {
            log.error("❌ Error fetching capacity from EMISS: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Получить объем отгруженной продукции
     *
     * @param okpd2Code код ОКПД2
     * @param year год
     * @return объем в млн рублей
     */
    @Cacheable(value = "emiss-shipped", key = "#okpd2Code + '-' + #year", unless = "#result == null")
    public Double getShippedProduction(String okpd2Code, Integer year) {
        try {
            rateLimiter.acquire("emiss");

            log.info("Fetching shipped production for OKPD2={}, Year={}", okpd2Code, year);

            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/data/" + SHIPPED_PRODUCTION)
                    .queryParam("startPeriod", year)
                    .queryParam("endPeriod", year)
                    .build()
                    .toUriString();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Double volume = parseSdmxResponse(response.getBody());

                if (volume != null) {
                    log.info("✅ Shipped production: {} млн руб.", volume);
                    return volume;
                }
            }

            log.warn("⚠️ No shipped production data found");
            return null;

        } catch (Exception e) {
            log.error("❌ Error fetching shipped production from EMISS: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Парсинг SDMX ответа (JSON или XML)
     */
    private Double parseSdmxResponse(String responseBody) {
        try {
            // Пытаемся распарсить как JSON
            if (responseBody.trim().startsWith("{")) {
                return parseSdmxJson(responseBody);
            }

            // Иначе как XML
            return parseSdmxXml(responseBody);

        } catch (Exception e) {
            log.error("Error parsing SDMX response: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Парсинг SDMX-JSON
     */
    private Double parseSdmxJson(String json) {
        // Простой парсинг JSON (можно использовать Jackson для полного разбора)
        try {
            // SDMX-JSON имеет структуру:
            // {"data": {"dataSets": [{"observations": {"0:0:0": [105.2]}}]}}

            int obsStart = json.indexOf("\"observations\"");
            if (obsStart == -1) return null;

            int valueStart = json.indexOf("[", obsStart);
            int valueEnd = json.indexOf("]", valueStart);

            if (valueStart != -1 && valueEnd != -1) {
                String value = json.substring(valueStart + 1, valueEnd).trim();
                return Double.parseDouble(value);
            }

        } catch (Exception e) {
            log.error("Error parsing SDMX JSON: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Парсинг SDMX-XML
     */
    private Double parseSdmxXml(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            // SDMX-XML имеет структуру:
            // <Obs><ObsValue value="105.2"/></Obs>

            NodeList nodes = doc.getElementsByTagName("ObsValue");
            if (nodes.getLength() > 0) {
                String value = nodes.item(0).getAttributes().getNamedItem("value").getNodeValue();
                return Double.parseDouble(value);
            }

        } catch (Exception e) {
            log.error("Error parsing SDMX XML: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Получить список доступных индикаторов по ОКПД2
     */
    public List<String> getAvailableIndicators(String okpd2Code) {
        // TODO: Реализовать поиск индикаторов по ОКПД2
        List<String> indicators = new ArrayList<>();
        indicators.add(INDUSTRIAL_PRODUCTION_INDEX);
        indicators.add(CAPACITY_UTILIZATION);
        indicators.add(SHIPPED_PRODUCTION);
        return indicators;
    }
}
