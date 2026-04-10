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
public class CreateFdaHeaderRequest {

    @NotNull(message = "Transaction Date is required")
    private LocalDate transactionDate;

    @NotNull(message = "Principal is required")
    private Long principalPoid;

    @Size(max = 50, message = "Principal Contact cannot exceed 50 characters")
    private String principalContact;

    private Long voyagePoid;

    private Long vesselPoid;

    private LocalDate arrivalDate;

    private LocalDate sailDate;

    private Long portPoid;

    @Size(max = 100, message = "Commodity Poid cannot exceed 100 characters")
    private String commodityPoid;

    @Size(max = 30, message = "Operation Type cannot exceed 30 characters")
    private String operationType;

    private BigDecimal importQty;

    private BigDecimal exportQty;

    private BigDecimal totalQuantity;

    @Size(max = 20, message = "Unit cannot exceed 20 characters")
    private String unit;

    @Size(max = 20, message = "Harbour Call Type cannot exceed 20 characters")
    private String harbourCallType;

    @Size(max = 20, message = "Currency Code cannot exceed 20 characters")
    private String currencyCode;

    private BigDecimal currencyRate;

    private Long costCentrePoid;

    @Size(max = 1, message = "Vessel Verified cannot exceed 1 character")
    private String vesselVerified;

    private LocalDate vesselVerifiedDate;

    @Size(max = 30, message = "Vessel Verified By cannot exceed 30 characters")
    private String vesselVerifiedBy;

    @Size(max = 1, message = "Urgent Approval cannot exceed 1 character")
    private String urgentApproval;

    private Long principalAprvlDays;

    @Size(max = 1, message = "Principal Approved cannot exceed 1 character")
    private String principalApproved;

    private LocalDate principalApprovedDate;

    @Size(max = 30, message = "Principal Approved By cannot exceed 30 characters")
    private String principalApprovedBy;

    private Long reminderMinutes;

    @Size(max = 100, message = "Cargo Details cannot exceed 100 characters")
    private String cargoDetails;

    @Size(max = 2000, message = "Remarks cannot exceed 2000 characters")
    private String remarks;

    private Long pdaRef;

    private Long addressPoid;

    @NotNull(message = "Salesman is required")
    private Long salesmanPoid;

    private BigDecimal transhipmentQty;

    @PositiveOrZero(message = "DWT must be >= 0")
    private BigDecimal dwt;

    @NotNull(message = "GRT is required")
    @PositiveOrZero(message = "GRT must be >= 0")
    private BigDecimal grt;

    @Size(max = 20, message = "IMO Number cannot exceed 20 characters")
    private String imoNumber;

    @PositiveOrZero(message = "NRT must be >= 0")
    private BigDecimal nrt;

    private BigDecimal numberOfDays;

    @Size(max = 100, message = "Port Description cannot exceed 100 characters")
    private String portDescription;

    private Long termsPoid;

    @Size(max = 30, message = "Vessel Type Poid cannot exceed 30 characters")
    private String vesselTypePoid;

    private Long linePoid;

    private Long printPrincipal;

    @Size(max = 30, message = "Voyage No cannot exceed 30 characters")
    private String voyageNo;

    @Size(max = 100, message = "Ref Type cannot exceed 100 characters")
    private String refType;

    @Size(max = 1, message = "Supplementary cannot exceed 1 character")
    private String supplementary;

    @Size(max = 300, message = "Business Ref By cannot exceed 300 characters")
    private String businessRefBy;

    @Size(max = 1, message = "FDA Without Charges cannot exceed 1 character")
    private String fdaWithoutCharges;

    private Long printBankPoid;

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

    private Long vesselHandledBy;

    private LocalDate vesselSailDate;

    @Valid
    private List<FdaChargeDto> charges;
}
