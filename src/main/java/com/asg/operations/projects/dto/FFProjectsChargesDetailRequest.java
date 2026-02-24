package com.asg.operations.projects.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FFProjectsChargesDetailRequest {

    private String actionType;
    private Long detRowId;
    private Long quotationReferencePoid;
    private Long chargePoid;
    private String printableChargeDescription;
    private Double quantity;
    private String unit;
    private String buyingCurrencyCode;
    private Double currencyRate;
    private Double buyingUnitRate;
    private Double buyingTotalBhd;
    private Double sellingUnitRate;
    private Double sellingTotal;
    private Long taxIdPoid;
    private Double taxPercentage;
    private Double taxAmount;
    private Double sellingGrandTotal;
    private Double sellingGrandTotalBhd;
    private Double marginBhd;
    private String remarks;
}
