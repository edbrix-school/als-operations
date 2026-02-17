package com.asg.operations.projects.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FFProjectsChargesDetailResponse {

    private Long transactionPoid;
    private Long detRowId;
    private Long quotationReferencePoid;
    private Long chargeDetailsPoid;
    private String printableChargeDescription;
    private String chargeBasis;
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
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
}
