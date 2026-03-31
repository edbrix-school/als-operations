package com.asg.operations.salesquotationprojects.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class SalesQuoteProjectsChargeDetailResponse {
    private Long transactionPoid;
    private Long detRowId;
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
    private String remarks;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
}