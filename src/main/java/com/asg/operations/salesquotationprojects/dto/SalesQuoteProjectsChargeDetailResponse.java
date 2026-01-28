package com.asg.operations.salesquotationprojects.dto;

import com.asg.common.lib.dto.LovGetListDto;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SalesQuoteProjectsChargeDetailResponse {
    private Long transactionPoid;
    private Long detRowId;
    private Long chargePoid;
    private LovGetListDto chargeDet;
    private String printableChargeDesc;
    private BigDecimal quantity;
    private Long unitPoid;
    private LovGetListDto unitDet;
    private String buyCurrencyCode;
    private LovGetListDto buyCurrencyDet;
    private BigDecimal buyCurrencyRate;
    private BigDecimal buyUnitRate;
    private BigDecimal buyTotalLc;
    private BigDecimal sellUnitRateFc;
    private BigDecimal sellTotalFc;
    private Long taxPoid;
    private LovGetListDto taxDet;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmountFc;
    private BigDecimal sellGrandTotalFc;
    private BigDecimal taxAmountLc;
    private BigDecimal sellGrandTotalLc;
    private String remarks;
    private String createdBy;
    private LocalDate createdDate;
    private String lastModifiedBy;
    private LocalDate lastModifiedDate;
}