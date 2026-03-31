package com.asg.operations.finaldisbursementaccount.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class UpdateFdaHeaderRequest {

    @NotNull(message = "Transaction Date is required")
    private LocalDate transactionDate;

    @NotNull(message = "Principal is required")
    private Long principalPoid;

    private BigDecimal importQty;

    private BigDecimal exportQty;

    private BigDecimal transhipmentQty;

    private BigDecimal totalQuantity;

    @NotNull(message = "Salesman is required")
    private Long salesmanPoid;

    private Long portPoid;

    @Size(max = 2000, message = "Remarks cannot exceed 2000 characters")
    private String remarks;

    @Size(max = 100, message = "Cargo Details cannot exceed 100 characters")
    private String cargoDetails;

    @Size(max = 30, message = "Operation Type cannot exceed 30 characters")
    private String operationType;

    @Size(max = 20, message = "Harbour Call Type cannot exceed 20 characters")
    private String harbourCallType;

    @Size(max = 20, message = "Unit cannot exceed 20 characters")
    private String unit;

    private BigDecimal numberOfDays;

    @Size(max = 100, message = "Port Description cannot exceed 100 characters")
    private String portDescription;

    @Size(max = 100, message = "FDA Sub Type cannot exceed 1 character")
    private String fdaSubType;

    @Size(max = 100, message = "Sub Category cannot exceed 100 characters")
    private String subCategory;

    @NotNull(message = "GRT is required")
    private BigDecimal grt;

    private BigDecimal nrt;
    private BigDecimal dwt;
    private Long printBankPoid;
    private List<FdaChargeDto> charges;
}
