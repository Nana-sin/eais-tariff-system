package com.ttp.evaluation.integration;

import java.util.List;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class WtoTariffInfo {
    private String hsCode;
    private String productDescription;
    private Double boundRate;
    private Double appliedRate;
    private String tariffType;
    private Boolean hasTariffQuota;
    private Double quotaVolume;
    private Double inQuotaRate;
    private Double outQuotaRate;
    private List<String> specialProvisions;
}
