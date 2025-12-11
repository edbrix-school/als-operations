package com.asg.operations.finaldisbursementaccount.dto;

import com.asg.operations.commonlov.dto.LovItem;
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
    private LovItem chargeDet;
    @Size(max = 20, message = "Currency Code cannot exceed 20 characters")
    private String currencyCode;
    private BigDecimal currencyRate;
    @Size(max = 30, message = "Details From cannot exceed 30 characters")
    private String detailsFrom;
    private LovItem detailsFromDet;
    @PositiveOrZero(message = "Qty must be >= 0")
    private BigDecimal qty;
    @PositiveOrZero(message = "Days must be >= 0")
    private BigDecimal days;
    @PositiveOrZero(message = "Rate must be >= 0")
    private BigDecimal pdaRate;
    private Long rateTypePoid;
    private LovItem rateTypeDet;
    @Size(max = 1, message = "Manual cannot exceed 1 character")
    private String manual;
    @PositiveOrZero(message = "Amount must be >= 0")
    private BigDecimal amount;
    @Size(max = 2000, message = "Remarks cannot exceed 2000 characters")
    private String remarks;

    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;

    @Size(max = 100, message = "Remark Qty Days cannot exceed 100 characters")
    private String remarkQtyDays;
    @PositiveOrZero(message = "Cost amount must be >= 0")
    private BigDecimal costAmount;
    @PositiveOrZero(message = "FDA amount must be >= 0")
    private BigDecimal fdaAmount;
    private Integer seqNo;
    @NotNull(message = "Principal is required")
    private Long principalPoid;
    private LovItem principalDet;
    private Long refDetRowId;
    @Size(max = 20, message = "Ref Doc Id cannot exceed 20 characters")
    private String refDocId;
    private Long refDocPoid;
    private LovItem refDocDet;
    @Size(max = 20, message = "Booked Doc Id cannot exceed 20 characters")
    private String bookedDocPoid;
    private LovItem bookedDocDet;
    @Size(max = 20, message = "Dn Doc Id cannot exceed 20 characters")
    private String dnDocId;
    @Size(max = 20, message = "Dn Doc Poid cannot exceed 20 characters")
    private String dnDocPoid;
    private LovItem dnDocDet;
    @Size(max = 500, message = "Print Remarks cannot exceed 500 characters")
    private String printRemarks;
    @Size(max = 30, message = "Dn From cannot exceed 30 characters")
    private String dnFrom;
    @Size(max = 20, message = "Cn Doc Id cannot exceed 20 characters")
    private String cnDocId;
    @Size(max = 20, message = "Cn Doc Poid cannot exceed 20 characters")
    private String cnDocPoid;
    private LovItem cnDocDet;
    private BigDecimal dnAmount;
    private BigDecimal cnAmount;
    @Size(max = 20, message = "Cn Det Row Id cannot exceed 20 character")
    private String cnDetRowId;
    @Size(max = 20, message = "Dn Det Row Id cannot exceed 20 character")
    private String dnDetRowId;
    private Long taxPoid;
    private LovItem taxDet;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private Long dnTaxPoid;
    private LovItem dnTaxDet;
    private BigDecimal dnTaxPercentage;
    private BigDecimal dnTaxAmount;
    private BigDecimal dnTotalAmount;
    private Long cnTaxPoid;
    private LovItem cnTaxDet;
    private BigDecimal cnTaxPercentage;
    private BigDecimal cnTaxAmount;
    private BigDecimal cnTotalAmount;
    private Long pdaPoid;
    private LovItem pdaDet;
    private Long pdaDetRowId;
    private Long printSeqNo;
    private BigDecimal profitLoss;
    private BigDecimal profitLossPer;
    private String actionType;
}
