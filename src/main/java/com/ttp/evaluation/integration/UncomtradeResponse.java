package com.ttp.evaluation.integration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Ответ от UN Comtrade Public API v1
 */
@Data
public class UncomtradeResponse {

    @JsonProperty("elapsedTime")
    private String elapsedTime;

    @JsonProperty("count")
    private Integer count;

    @JsonProperty("data")
    private List<TradeData> data;

    @Data
    public static class TradeData {

        @JsonProperty("typeCode")
        private String typeCode; // C = Commodities, S = Services

        @JsonProperty("freqCode")
        private String freqCode; // A = Annual, M = Monthly

        @JsonProperty("refPeriodId")
        private Integer refPeriodId; // Year or YYYYMM

        @JsonProperty("refYear")
        private Integer refYear;

        @JsonProperty("refMonth")
        private Integer refMonth;

        @JsonProperty("reporterCode")
        private Integer reporterCode;

        @JsonProperty("reporterDesc")
        private String reporterDesc;

        @JsonProperty("reporterISO")
        private String reporterISO;

        @JsonProperty("flowCode")
        private String flowCode; // M = Import, X = Export

        @JsonProperty("flowDesc")
        private String flowDesc;

        @JsonProperty("partnerCode")
        private Integer partnerCode;

        @JsonProperty("partnerDesc")
        private String partnerDesc;

        @JsonProperty("partnerISO")
        private String partnerISO;

        @JsonProperty("cmdCode")
        private String cmdCode; // Commodity code (HS)

        @JsonProperty("cmdDesc")
        private String cmdDesc;

        @JsonProperty("primaryValue")
        private Double primaryValue; // Trade value in USD

        @JsonProperty("netWgt")
        private Double netWgt; // Net weight in kg

        @JsonProperty("qty")
        private Double qty; // Quantity

        @JsonProperty("qtyUnitCode")
        private Integer qtyUnitCode;

        @JsonProperty("qtyUnitAbbr")
        private String qtyUnitAbbr;
    }
}
