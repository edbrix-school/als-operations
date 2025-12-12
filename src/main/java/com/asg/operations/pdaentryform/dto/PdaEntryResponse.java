package com.asg.operations.pdaentryform.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for PDA Entry Form operations
 */
import com.asg.operations.commonlov.dto.LovItem;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdaEntryResponse {

    // Response-only fields
    private Long transactionPoid;
    private String docRef;
    private String transactionRef;
    private String deleted;

    // All fields from Request
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    private BigDecimal principalPoid;
    private LovItem principalDet;
    private String principalName;
    private String principalContact;
    private BigDecimal voyagePoid;
    private LovItem voyageDet;
    private String voyageNo;
    private BigDecimal vesselPoid;
    private LovItem vesselDet;
    private BigDecimal vesselTypePoid;
    private LovItem vesselTypeDet;
    private BigDecimal grt;
    private BigDecimal nrt;
    private BigDecimal dwt;
    private String imoNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate arrivalDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate sailDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualArrivalDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualSailDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate vesselSailDate;

    private BigDecimal portPoid;
    private LovItem portDet;
    private String portDescription;
    private BigDecimal linePoid;
    private LovItem lineDet;
    private String comodityPoid;
    private LovItem comodityDet;
    private String operationType;
    private LovItem operationTypeDet;
    private String harbourCallType;
    private BigDecimal importQty;
    private BigDecimal exportQty;
    private BigDecimal transhipmentQty;
    private BigDecimal totalQuantity;
    private String unit;
    private LovItem unitDet;
    private BigDecimal numberOfDays;
    private String currencyCode;
    private LovItem currencyDet;
    private BigDecimal currencyRate;
    private BigDecimal totalAmount;
    private BigDecimal costCentrePoid;
    private BigDecimal salesmanPoid;
    private LovItem salesmanDet;
    private BigDecimal termsPoid;
    private BigDecimal addressPoid;
    private String refType;
    private LovItem refTypeDet;
    private String subCategory;
    private LovItem subCategoryDet;
    private String status;
    private String cargoDetails;
    private String remarks;
    private String vesselVerified;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate vesselVerifiedDate;

    private String vesselVerifiedBy;
    private BigDecimal vesselHandledBy;
    private LovItem vesselHandledByDet;
    private String urgentApproval;
    private String principalApproved;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate principalApprovedDate;

    private String principalApprovedBy;
    private BigDecimal principalAprvlDays;
    private BigDecimal reminderMinutes;
    private BigDecimal printPrincipal;
    private LovItem printPrincipalDet;
    private String fdaRef;
    private BigDecimal fdaPoid;
    private String multipleFda;
    private String nominatedPartyType;
    private LovItem nominatedPartyTypeDet;
    private BigDecimal nominatedPartyPoid;
    private LovItem nominatedPartyDet;
    private BigDecimal bankPoid;
    private LovItem bankDet;
    private String businessRefBy;
    private String pmiDocument;
    private String cancelRemark;
    private String menasDues;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate documentSubmittedDate;

    private String documentSubmittedBy;
    private String documentSubmittedStatus;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate documentReceivedDate;

    private String documentReceivedFrom;
    private String documentReceivedStatus;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate submissionAcceptedDate;

    private String submissionAcceptedBy;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate verificationAcceptedDate;

    private String verificationAcceptedBy;
    private String acctsCorrectionRemarks;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate acctsReturnedDate;

    // Audit fields
    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;

    private String lastModifiedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastModifiedDate;

    // Nested detail lists (optional - can be loaded separately)
    private List<PdaEntryChargeDetailResponse> chargeDetails;
    private List<PdaEntryVehicleDetailResponse> vehicleDetails;
    private List<PdaEntryTdrDetailResponse> tdrDetails;
    private List<PdaEntryAcknowledgmentDetailResponse> acknowledgmentDetails;
}

