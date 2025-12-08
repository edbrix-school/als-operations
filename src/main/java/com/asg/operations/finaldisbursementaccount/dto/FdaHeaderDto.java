package com.asg.operations.finaldisbursementaccount.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class FdaHeaderDto {
    private Long transactionPoid;
    private LocalDate transactionDate;
    private Long groupPoid;
    private Long companyPoid;

    @NotNull(message = "Principal is required")
    private Long principalPoid;

    private String principalContact;
    private String docRef;
    private Long voyagePoid;
    private Long vesselPoid;
    private LocalDate arrivalDate;
    private LocalDate sailDate;

    @NotNull(message = "Port is required")
    private Long portPoid;

    private String commodityPoid;
    private String operationType;
    private BigDecimal importQty;
    private BigDecimal exportQty;
    private BigDecimal totalQuantity;
    private String unit;
    private String harbourCallType;
    private String currencyCode;
    private BigDecimal currencyRate;
    private Long costCentrePoid;
    private String vesselVerified;
    private LocalDate vesselVerifiedDate;
    private String vesselVerifiedBy;
    private String urgentApproval;
    private Long principalAprvlDays;
    private String principalApproved;
    private LocalDate principalApprovedDate;
    private String principalApprovedBy;
    private Long reminderMinutes;
    private String cargoDetails;
    private String status;
    private LocalDate fdaClosedDate;

    @Size(max = 2000, message = "Remarks cannot exceed 2000 characters")
    private String remarks;

    private BigDecimal totalAmount;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private String deleted;
    private String pdaRef;
    private Long addressPoid;

    @NotNull(message = "Salesman is required")
    private Long salesmanPoid;

    private BigDecimal transhipmentQty;
    @PositiveOrZero(message = "DWT must be >= 0")
    private BigDecimal dwt;

    @NotNull(message = "GRT is required")
    @PositiveOrZero(message = "GRT must be >= 0")
    private BigDecimal grt;

    private String imoNumber;
    @PositiveOrZero(message = "NRT must be >= 0")
    private BigDecimal nrt;
    private BigDecimal numberOfDays;
    private String portDescription;
    private Long termsPoid;
    private String vesselTypePoid;
    private Long linePoid;
    private Long printPrincipal;
    private String voyageNo;
    private BigDecimal profitLossAmount;
    private String profitLossPer;
    private String fdaClosingBy;
    private LocalDate glClosingDate;
    private String refType;
    private String closedRemark;
    private String supplementary;
    private Long supplementaryFdaPoid;
    private String businessRefBy;
    private String fdaWithoutCharges;
    private Long printBankPoid;
    private String portCallNumber;
    private String nominatedPartyType;
    private Long nominatedPartyPoid;
    private LocalDate documentSubmittedDate;
    private String documentSubmittedBy;
    private String documentSubmittedStatus;
    private String fdaSubType;
    private String subCategory;
    private LocalDate documentReceivedDate;
    private String documentReceivedFrom;
    private String documentReceivedStatus;
    private LocalDate submissionAcceptedDate;
    private LocalDate verificationAcceptedDate;
    private String submissionAcceptedBy;
    private String verificationAcceptedBy;
    private Long vesselHandledBy;
    private LocalDate vesselSailDate;
    private String accountsVerified;
    private String opsCorrectionRemarks;
    private LocalDate opsReturnedDate;
    private BigDecimal profitTotal;
    private BigDecimal lossTotal;

    private List<FdaChargeDto> charges;
}
