package com.alsharif.operations.finaldisbursementaccount.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class FdaChargeDto {

    private Long transactionPoid;
    private Long detRowId;

    @NotNull(message = "Charge type is required")
    private Long chargePoid;

    private String currencyCode;
    private BigDecimal currencyRate;
    private String detailsFrom;

    @PositiveOrZero(message = "Qty must be >= 0")
    private BigDecimal qty;

    @PositiveOrZero(message = "Days must be >= 0")
    private BigDecimal days;

    @PositiveOrZero(message = "Rate must be >= 0")
    private BigDecimal pdaRate;

    private Long rateTypePoid;
    private String manual;

    @PositiveOrZero(message = "Amount must be >= 0")
    private BigDecimal amount;

    @Size(max = 2000, message = "Remarks cannot exceed 2000 characters")
    private String remarks;

    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private String remarkQtyDays;

    @PositiveOrZero(message = "Cost amount must be >= 0")
    private BigDecimal costAmount;

    @PositiveOrZero(message = "FDA amount must be >= 0")
    private BigDecimal fdaAmount;

    private Integer seqNo;

    @NotNull(message = "Principal is required")
    private Long principalPoid;

    private Long refDetRowId;
    private String refDocId;
    private Long refDocPoid;
    private String bookedDocPoid;
    private String dnDocId;
    private String dnDocPoid;
    private String printRemarks;
    private String dnFrom;
    private String cnDocId;
    private String cnDocPoid;
    private BigDecimal dnAmount;
    private BigDecimal cnAmount;
    private String cnDetRowId;
    private String dnDetRowId;
    private Long taxPoid;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private Long dnTaxPoid;
    private BigDecimal dnTaxPercentage;
    private BigDecimal dnTaxAmount;
    private BigDecimal dnTotalAmount;
    private Long cnTaxPoid;
    private BigDecimal cnTaxPercentage;
    private BigDecimal cnTaxAmount;
    private BigDecimal cnTotalAmount;
    private Long pdaPoid;
    private Long pdaDetRowId;
    private Long printSeqNo;
    private BigDecimal profitLoss;
    private BigDecimal profitLossPer;
    private String actionType;
}
