package com.ttp.evaluation.integration;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class UnComtradeRecord {
    private String reporterCode;
    private String reporterDesc;
    private String partnerCode;
    private String partnerDesc;
    private String cmdCode;
    private String cmdDesc;
    private String flowCode;
    private String flowDesc;
    private Integer period;
    private Double primaryValue;
    private Double netWeight;
    private Double qty;
    private String qtyUnitCode;
}
