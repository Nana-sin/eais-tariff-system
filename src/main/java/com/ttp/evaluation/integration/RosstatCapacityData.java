package com.ttp.evaluation.integration;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class RosstatCapacityData {
    private String okpd2Code;
    private Integer year;
    private Double capacityUtilization;
    private Double totalCapacity;
    private Double actualProduction;
}
