package com.asg.operations.finaldisbursementaccount.dto;

import com.asg.operations.commonlov.dto.LovItem;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FdaChargeDto {

    private Long transactionPoid;
    private Long detRowId;

    private Long chargePoid;

    private LovItem chargeDet;

//    @Size(max = 30, message = "Details From cannot exceed 30 characters")
//    private String detailsFrom;
//
//    private LovItem detailsFromDet;
//
//    @PositiveOrZero(message = "Qty must be >= 0")
//    private BigDecimal qty;
//
//    @PositiveOrZero(message = "Days must be >= 0")
//    private BigDecimal days;
//
//    @PositiveOrZero(message = "Rate must be >= 0")
//    private BigDecimal pdaRate;
//
//    @PositiveOrZero(message = "Amount must be >= 0")
//    private BigDecimal amount;

    @Size(max = 2000, message = "Remarks cannot exceed 2000 characters")
    private String remarks;
//
//    private String createdBy;
//
//    private LocalDateTime createdDate;
//
//    private String lastModifiedBy;
//
//    private LocalDateTime lastModifiedDate;
//
//    @Size(max = 100, message = "Remark Qty Days cannot exceed 100 characters")
//    private String remarkQtyDays;
//
//    @PositiveOrZero(message = "Cost amount must be >= 0")
//    private BigDecimal costAmount;
//
//    @PositiveOrZero(message = "FDA amount must be >= 0")
//    private BigDecimal fdaAmount;

    private Integer seqNo;

    private Long principalPoid;

    private LovItem principalDet;

    @Size(max = 500, message = "Print Remarks cannot exceed 500 characters")
    private String printRemarks;

//    private BigDecimal dnAmount;
//
//    private BigDecimal cnAmount;
//
//    private BigDecimal dnTaxAmount;
//
//    private BigDecimal dnTotalAmount;
//
//    private BigDecimal cnTaxAmount;

    private Long printSeqNo;

//    private BigDecimal profitLoss;
//
//    private BigDecimal profitLossPer;

    private String actionType;
}
