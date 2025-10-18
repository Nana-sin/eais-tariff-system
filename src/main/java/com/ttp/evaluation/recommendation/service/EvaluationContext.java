package com.ttp.evaluation.recommendation.service;

import com.ttp.evaluation.integration.UnComtradeRecord;
import com.ttp.evaluation.integration.WtoSchedulesClient;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Контекст для оценки мер ТТП
 * Содержит все необходимые данные из внешних источников
 */
@Data
@Builder
public class EvaluationContext {
    private String hsCode;
    private String tnVedCode;
    private String okpd2Code;

    // Trade data
    private double chinaImportShare;
    private double unfriendlyCountriesShare;
    private double totalImportShare;
    private boolean importStable;

    // Production data
    private double productionDecline;
    private double capacityUtilization;

    // Tariff data
    private WtoSchedulesClient.WtoTariffInfo tariffInfo;
    private Boolean hasTariffBinding;
    private Double tariffMargin;
    private Boolean canRaiseTariff;

    // Historical data
    private List<UnComtradeRecord> importHistory;

    // Price data
    private Double averageImportPrice;
    private Double domesticPrice;
}