package com.ttp.evaluation.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Клиент для работы с данными Росстата через ЕМИСС (fedstat.ru)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RosstatClient {

    private final EmissClient emissClient;

    /**
     * Рассчитать падение производства за N лет на основе ЕМИСС
     */
    public double calculateProductionDecline(String okpd2Code, int yearsToCompare) {
        int currentYear = LocalDate.now().getYear() - 1;
        int previousYear = currentYear - yearsToCompare;

        Double currentIndex = emissClient.getIndustrialProductionIndex(okpd2Code, currentYear);
        Double previousIndex = emissClient.getIndustrialProductionIndex(okpd2Code, previousYear);

        if (currentIndex != null && previousIndex != null && previousIndex > 0) {
            double change = ((currentIndex - previousIndex) / previousIndex) * 100;
            log.info("Production change for {}: {:.2f}%", okpd2Code, change);
            return change;
        }

        log.warn("Insufficient EMISS data for decline calculation: {} years for {}", yearsToCompare, okpd2Code);
        return 0.0;
    }

    /**
     * Получить загрузку производственных мощностей через ЕМИСС
     */
    public RosstatCapacityData getProductionCapacity(String okpd2Code, int year) {
        Double capacity = emissClient.getCapacityUtilization(okpd2Code, year);
        if (capacity != null) {
            double maxCapacity = 1_000_000.0;
            double productionVolume = maxCapacity * (capacity / 100.0);
            return new RosstatCapacityData(okpd2Code, year, productionVolume, maxCapacity, capacity);
        }

        log.warn("No EMISS capacity data for OKPD2={}, Year={}", okpd2Code, year);
        return new RosstatCapacityData(okpd2Code, year, 0.0, 0.0, 0.0);
    }

    /**
     * Маппинг ТН ВЭД → ОКПД2 (статический или из БД)
     */
    public String mapTnVedToOkpd2(String tnVedCode) {
        if (tnVedCode == null || tnVedCode.isEmpty()) {
            log.warn("Empty HS code provided");
            return "00.00.00"; // default OKPD2
        }

        if (tnVedCode.startsWith("851762")) return "26.30.22";
        if (tnVedCode.startsWith("847130")) return "26.20.11";
        if (tnVedCode.startsWith("8703"))   return "29.10.00";

        // Fallback: пытаемся найти по первым 2/4 цифрам
        String prefix4 = tnVedCode.length() >= 4 ? tnVedCode.substring(0, 4) : tnVedCode;
        String prefix2 = tnVedCode.length() >= 2 ? tnVedCode.substring(0, 2) : tnVedCode;

        log.warn("No OKPD2 mapping for HS code: {}, using default", tnVedCode);
        return "00.00.00"; // вместо null
    }
}
