package com.asg.operations.finaldisbursementaccount.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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

    @Size(max = 20, message = "Currency Code cannot exceed 20 characters")
    private String currencyCode;

    private BigDecimal currencyRate;

    private Long portPoid;

    @Size(max = 1, message = "Accounts Verified cannot exceed 1 character")
    private String accountsVerified;

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

    @Size(max = 100, message = "Port Call Number cannot exceed 100 characters")
    private String portCallNumber;

    @Size(max = 100, message = "Nominated Party Type cannot exceed 100 characters")
    private String nominatedPartyType;

    @NotNull(message = "Nominated Party is required. Select either Principal or Customer")
    private Long nominatedPartyPoid;

    @Size(max = 100, message = "FDA Sub Type cannot exceed 100 character")
    private String fdaSubType;

    @Size(max = 100, message = "Sub Category cannot exceed 100 characters")
    private String subCategory;

    @NotNull(message = "GRT is required")
    @PositiveOrZero(message = "GRT must be >= 0")
    private BigDecimal grt;

    private BigDecimal nrt;
    private BigDecimal dwt;
    private Long printBankPoid;

    private LocalDate arrivalDate;

    private LocalDate sailDate;

    private Long vesselHandledBy;

    private LocalDate vesselSailDate;

    @Valid
    private List<FdaChargeDto> charges;
}
