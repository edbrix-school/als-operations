package com.asg.operations.pdaentryform.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdaEntryRequest {

    private LocalDate transactionDate;

    private Long principalPoid;

    @Size(max = 500)
    private String principalName;

    @Size(max = 50)
    private String principalContact;

    private Long voyagePoid;

    @Size(max = 30)
    private String voyageNo;

    private Long vesselPoid;

    private Long vesselTypePoid;

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

    private Long portPoid;

    @Size(max = 100)
    private String portDescription;

    private Long linePoid;

    @Size(max = 50)
    private String comodityPoid;

    @Size(max = 30)
    private String operationType;

    @Size(max = 20)
    private String harbourCallType;

    private Long importQty;

    private Long exportQty;

    private Long transhipmentQty;

    private Long totalQuantity;

    @Size(max = 20)
    private String unit;

    private Long numberOfDays;

    @Size(max = 20)
    private String currencyCode;

    private BigDecimal currencyRate;

    private BigDecimal totalAmount;

    private Long costCentrePoid;

    private Long salesmanPoid;

    private Long termsPoid;

    private Long addressPoid;

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

    private Long vesselHandledBy;

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

    private Long printPrincipal;

    @Size(max = 100)
    private String fdaRef;

    private Long fdaPoid;

    @Size(max = 1)
    private String multipleFda;

    @Size(max = 100)
    private String nominatedPartyType;

    private Long nominatedPartyPoid;

    private Long bankPoid;

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

