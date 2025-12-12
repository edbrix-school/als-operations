package com.asg.operations.finaldisbursementaccount.dto;

import com.asg.operations.commonlov.dto.LovItem;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
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
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;
    private Long groupPoid;
    private LovItem groupDet;
    private Long companyPoid;
    private LovItem companyDet;
    @NotNull(message = "Principal is required")
    private Long principalPoid;
    private LovItem principalDet;
    @Size(max = 50, message = "Principal Contact cannot exceed 50 characters")
    private String principalContact;
    @Size(max = 25, message = "Document Reference cannot exceed 25 characters")
    private String docRef;
    private Long voyagePoid;
    private LovItem voyageDet;
    private Long vesselPoid;
    private LovItem vesselDet;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate arrivalDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate sailDate;
    @NotNull(message = "Port is required")
    private Long portPoid;
    private LovItem portDet;
    @Size(max = 100, message = "Commodity Poid cannot exceed 100 characters")
    private String commodityPoid;
    private LovItem commodityDet;
    @Size(max = 30, message = "Operation Type cannot exceed 30 characters")
    private String operationType;
    private LovItem operationTypeDet;
    private BigDecimal importQty;
    private BigDecimal exportQty;
    private BigDecimal totalQuantity;
    @Size(max = 20, message = "Unit cannot exceed 20 characters")
    private String unit;
    private LovItem unitDet;
    @Size(max = 20, message = "Harbour Call Type cannot exceed 20 characters")
    private String harbourCallType;
    @Size(max = 20, message = "Currency Code cannot exceed 20 characters")
    private String currencyCode;
    private BigDecimal currencyRate;
    private Long costCentrePoid;
    private LovItem costCentreDet;
    @Size(max = 1, message = "Vessel Verified cannot exceed 1 character")
    private String vesselVerified;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate vesselVerifiedDate;
    @Size(max = 30, message = "Vessel Verified By cannot exceed 30 characters")
    private String vesselVerifiedBy;
    @Size(max = 1, message = "Urgent Approval cannot exceed 1 character")
    private String urgentApproval;
    private Long principalAprvlDays;
    @Size(max = 1, message = "Principal Approved cannot exceed 1 character")
    private String principalApproved;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate principalApprovedDate;
    @Size(max = 30, message = "Principal Approved By cannot exceed 30 characters")
    private String principalApprovedBy;
    private Long reminderMinutes;
    @Size(max = 100, message = "Cargo Details cannot exceed 100 characters")
    private String cargoDetails;
    @Size(max = 30, message = "Status cannot exceed 30 characters")
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fdaClosedDate;
    @Size(max = 2000, message = "Remarks cannot exceed 2000 characters")
    private String remarks;
    private BigDecimal totalAmount;
    private String createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime lastModifiedDate;
    @Size(max = 1, message = "Deleted cannot exceed 1 character")
    private String deleted;
    @Size(max = 100, message = "PDA Reference cannot exceed 100 characters")
    private String pdaRef;
    private LovItem pdaRefDet;
    private Long addressPoid;
    private LovItem addressDet;
    @NotNull(message = "Salesman is required")
    private Long salesmanPoid;
    private LovItem salesmanDet;
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
    private LovItem termsDet;
    @Size(max = 30, message = "Vessel Type Poid cannot exceed 30 characters")
    private String vesselTypePoid;
    private LovItem vesselTypeDet;
    private Long linePoid;
    private LovItem lineDet;
    private Long printPrincipal;
    @Size(max = 30, message = "Voyage No cannot exceed 30 characters")
    private String voyageNo;
    private BigDecimal profitLossAmount;
    @Size(max = 50, message = "Profit Loss Per cannot exceed 50 characters")
    private String profitLossPer;
    @Size(max = 100, message = "FDA Closing By cannot exceed 100 characters")
    private String fdaClosingBy;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate glClosingDate;
    @Size(max = 100, message = "Ref Type cannot exceed 100 characters")
    private String refType;
    @Size(max = 500, message = "Closed Remark cannot exceed 500 characters")
    private String closedRemark;
    @Size(max = 1, message = "Supplementary cannot exceed 1 character")
    private String supplementary;
    private Long supplementaryFdaPoid;
    private LovItem supplementaryFdaDet;
    @Size(max = 300, message = "Business Ref By cannot exceed 300 characters")
    private String businessRefBy;
    @Size(max = 1, message = "FDA Without Charges cannot exceed 1 character")
    private String fdaWithoutCharges;
    private Long printBankPoid;
    private LovItem printBankDet;
    @Size(max = 100, message = "Port Call Number cannot exceed 100 characters")
    private String portCallNumber;
    @Size(max = 100, message = "Nominated Party Type cannot exceed 100 characters")
    private String nominatedPartyType;
    private LovItem nominatedPartyTypeDet;
    private Long nominatedPartyPoid;
    private LovItem nominatedPartyDet;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate documentSubmittedDate;
    @Size(max = 300, message = "Document Submitted By cannot exceed 300 characters")
    private String documentSubmittedBy;
    @Size(max = 300, message = "Document Submitted Status cannot exceed 300 characters")
    private String documentSubmittedStatus;
    @Size(max = 100, message = "FDA Sub Type cannot exceed 1 character")
    private String fdaSubType;
    @Size(max = 100, message = "Sub Category cannot exceed 100 characters")
    private String subCategory;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate documentReceivedDate;
    @Size(max = 300, message = "Document Received From cannot exceed 300 characters")
    private String documentReceivedFrom;
    @Size(max = 300, message = "Document Received Status cannot exceed 300 characters")
    private String documentReceivedStatus;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate submissionAcceptedDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate verificationAcceptedDate;
    @Size(max = 300, message = "Submission Accepted By cannot exceed 300 characters")
    private String submissionAcceptedBy;
    @Size(max = 300, message = "Verification Accepted By cannot exceed 300 characters")
    private String verificationAcceptedBy;
    private Long vesselHandledBy;
    private LovItem vesselHandledByDet;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate vesselSailDate;
    @Size(max = 1, message = "Accounts Verified cannot exceed 1 character")
    private String accountsVerified;
    @Size(max = 1000, message = "Ops Correction Remarks cannot exceed 1000 characters")
    private String opsCorrectionRemarks;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate opsReturnedDate;
    private BigDecimal profitTotal;
    private BigDecimal lossTotal;

    @Valid
    private List<FdaChargeDto> charges;
}
