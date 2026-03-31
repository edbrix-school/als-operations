package com.asg.operations.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargeDTO {
    private Long detRowId;
    private Long chargePoid;
    private String chargeCode;
    private String printableChargeDesc;
    private BigDecimal quantity;
    private String unit;
    private String buyingCurrencyCode;
    private BigDecimal currencyRate;
    private BigDecimal buyingUnitRate;
    private BigDecimal buyingTotalBhd;
    private BigDecimal sellingUnitRate;
    private BigDecimal sellingTotal;
    private Long taxPoid;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private BigDecimal sellingGrandTotal;
    private BigDecimal sellingGrandTotalBhd;
    private BigDecimal marginBhd;
    private String remarks;
}
