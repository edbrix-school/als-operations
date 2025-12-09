package com.asg.operations.pdaentryform.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdaEntryRequest {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    private BigDecimal principalPoid;

    @Size(max = 500)
    private String principalName;

    @Size(max = 50)
    private String principalContact;

    private BigDecimal voyagePoid;

    @Size(max = 30)
    private String voyageNo;

    private BigDecimal vesselPoid;

    private BigDecimal vesselTypePoid;

    private BigDecimal grt;

    private BigDecimal nrt;

    private BigDecimal dwt;

    @Size(max = 20)
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

    @Size(max = 100)
    private String portDescription;

    private BigDecimal linePoid;

    @Size(max = 50)
    private String comodityPoid;

    @Size(max = 30)
    private String operationType;

    @Size(max = 20)
    private String harbourCallType;

    private BigDecimal importQty;

    private BigDecimal exportQty;

    private BigDecimal transhipmentQty;

    private BigDecimal totalQuantity;

    @Size(max = 20)
    private String unit;

    private BigDecimal numberOfDays;

    @Size(max = 20)
    private String currencyCode;

    private BigDecimal currencyRate;

    private BigDecimal totalAmount;

    private BigDecimal costCentrePoid;

    private BigDecimal salesmanPoid;

    private BigDecimal termsPoid;

    private BigDecimal addressPoid;

    @NotNull(message = "Ref type is mandatory")
    @Size(max = 100)
    private String refType;

    @Size(max = 100)
    private String subCategory;

    @Size(max = 30)
    private String status;

    @Size(max = 100)
    private String cargoDetails;

    @Size(max = 2000)
    private String remarks;

    @Size(max = 1)
    private String vesselVerified;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate vesselVerifiedDate;

    @Size(max = 30)
    private String vesselVerifiedBy;

    private BigDecimal vesselHandledBy;

    @Size(max = 1)
    private String urgentApproval;

    @Size(max = 1)
    private String principalApproved;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate principalApprovedDate;

    @Size(max = 30)
    private String principalApprovedBy;

    private BigDecimal principalAprvlDays;

    private BigDecimal reminderMinutes;

    private BigDecimal printPrincipal;

    @Size(max = 100)
    private String fdaRef;

    private BigDecimal fdaPoid;

    @Size(max = 1)
    private String multipleFda;

    @Size(max = 100)
    private String nominatedPartyType;

    private BigDecimal nominatedPartyPoid;

    private BigDecimal bankPoid;

    @Size(max = 300)
    private String businessRefBy;

    @Size(max = 1)
    private String pmiDocument;

    @Size(max = 500)
    private String cancelRemark;

    @Size(max = 1)
    private String menasDues;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate documentSubmittedDate;

    @Size(max = 300)
    private String documentSubmittedBy;

    @Size(max = 300)
    private String documentSubmittedStatus;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate documentReceivedDate;

    @Size(max = 300)
    private String documentReceivedFrom;

    @Size(max = 300)
    private String documentReceivedStatus;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate submissionAcceptedDate;

    @Size(max = 300)
    private String submissionAcceptedBy;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate verificationAcceptedDate;

    @Size(max = 300)
    private String verificationAcceptedBy;

    @Size(max = 1000)
    private String acctsCorrectionRemarks;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate acctsReturnedDate;

}

