package com.asg.operations.projectjob.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProjectLoadDtlRow {

    private Long transactionPoid;
    private Long detRowId;
    private Long quotationRefPoid;
    private Long chargePoid;
    private String printableChargeDesc;
    private BigDecimal quantity;
    private Long unitPoid;
    private String buyCurrencyCode;
    private BigDecimal buyCurrencyRate;
    private BigDecimal buyUnitRate;
    private BigDecimal buyTotalLc;
    private BigDecimal sellUnitRateFc;
    private BigDecimal sellTotalFc;
    private Long taxPoid;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmountFc;
    private BigDecimal sellGrandTotalFc;
    private BigDecimal taxAmountLc;
    private BigDecimal sellGrandTotalLc;
    private BigDecimal marginAmountLc;
    private String remarks;
}
