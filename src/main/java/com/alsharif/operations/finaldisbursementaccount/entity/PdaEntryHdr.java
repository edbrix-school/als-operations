package com.alsharif.operations.finaldisbursementaccount.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "PDA_ENTRY_HDR")
public class PdaEntryHdr {

    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    private Long transactionPoid;

    @Column(name = "TRANSACTION_DATE", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "PRINCIPAL_POID")
    private Long principalPoid;

    @Column(name = "PRINCIPAL_CONTACT", length = 50)
    private String principalContact;

    @Column(name = "TRANSACTION_REF", length = 30, unique = true)
    private String transactionRef;

    @Column(name = "VOYAGE_POID")
    private Long voyagePoid;

    @Column(name = "VESSEL_POID")
    private Long vesselPoid;

    @Column(name = "ARRIVAL_DATE")
    private LocalDate arrivalDate;

    @Column(name = "SAIL_DATE")
    private LocalDate sailDate;

    @Column(name = "PORT_POID")
    private Long portPoid;

    @Column(name = "COMODITY_POID", length = 50)
    private String comodityPoid;

    @Column(name = "OPERATION_TYPE", length = 30)
    private String operationType;

    @Column(name = "IMPORT_QTY")
    private BigDecimal importQty;

    @Column(name = "EXPORT_QTY")
    private BigDecimal exportQty;

    @Column(name = "TOTAL_QUANTITY")
    private BigDecimal totalQuantity;

    @Column(name = "UNIT", length = 20)
    private String unit;

    @Column(name = "HARBOUR_CALL_TYPE", length = 20)
    private String harbourCallType;

    @Column(name = "CURRENCY_CODE", length = 20)
    private String currencyCode;

    @Column(name = "CURRENCY_RATE")
    private BigDecimal currencyRate;

    @Column(name = "COST_CENTRE_POID")
    private Long costCentrePoid;

    @Column(name = "VESSEL_VERIFIED", length = 1)
    private String vesselVerified = "N";

    @Column(name = "VESSEL_VERIFIED_DATE")
    private LocalDate vesselVerifiedDate;

    @Column(name = "VESSEL_VERIFIED_BY", length = 30)
    private String vesselVerifiedBy;

    @Column(name = "URGENT_APPROVAL", length = 1)
    private String urgentApproval = "N";

    @Column(name = "PRINCIPAL_APRVL_DAYS")
    private BigDecimal principalAprvlDays;

    @Column(name = "PRINCIPAL_APPROVED", length = 1)
    private String principalApproved = "N";

    @Column(name = "PRINCIPAL_APPROVED_DATE")
    private LocalDate principalApprovedDate;

    @Column(name = "PRINCIPAL_APPROVED_BY", length = 30)
    private String principalApprovedBy;

    @Column(name = "REMINDER_MINUTES")
    private BigDecimal reminderMinutes;

    @Column(name = "REMARKS", length = 2000)
    private String remarks;

    @Column(name = "TOTAL_AMOUNT")
    private BigDecimal totalAmount;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @Column(name = "DELETED", length = 1)
    private String deleted = "N";

    @Column(name = "CARGO_DETAILS", length = 100)
    private String cargoDetails;

    @Column(name = "STATUS", length = 30)
    private String status;

    @Column(name = "OLD_PORT_CODE", length = 20)
    private String oldPortCode;

    @Column(name = "OLD_VESSEL_CODE", length = 20)
    private String oldVesselCode;

    @Column(name = "OLD_PRINCIPAL_CODE", length = 20)
    private String oldPrincipalCode;

    @Column(name = "OLD_VOYAGE_JOB", length = 20)
    private String oldVoyageJob;

    @Column(name = "DOC_REF", length = 25, unique = true)
    private String docRef;

    @Column(name = "ADDRESS_POID")
    private Long addressPoid;

    @Column(name = "SALESMAN_POID")
    private Long salesmanPoid;

    @Column(name = "TRANSHIPMENT_QTY")
    private BigDecimal transhipmentQty;

    @Column(name = "DWT")
    private BigDecimal dwt;

    @Column(name = "GRT")
    private BigDecimal grt;

    @Column(name = "NRT")
    private BigDecimal nrt;

    @Column(name = "VESSEL_TYPE_POID")
    private Long vesselTypePoid;

    @Column(name = "IMO_NUMBER", length = 20)
    private String imoNumber;

    @Column(name = "NUMBER_OF_DAYS")
    private BigDecimal numberOfDays;

    @Column(name = "PORT_DESCRIPTION", length = 100)
    private String portDescription;

    @Column(name = "TERMS_POID")
    private Long termsPoid;

    @Column(name = "PRINT_PRINCIPAL")
    private BigDecimal printPrincipal;

    @Column(name = "VOYAGE_NO", length = 30)
    private String voyageNo;

    @Column(name = "LINE_POID")
    private Long linePoid;

    @Column(name = "FDA_REF", length = 100)
    private String fdaRef;

    @Column(name = "CANCEL_REMARK", length = 500)
    private String cancelRemark;

    @Column(name = "REF_TYPE", length = 100)
    private String refType;

    @Column(name = "PMI_DOCUMENT", length = 1)
    private String pmiDocument = "N";

    @Column(name = "BUSINESS_REF_BY", length = 300)
    private String businessRefBy;

    @Column(name = "BANK_POID")
    private Long bankPoid;

    @Column(name = "PORT_CALL_NUMBER", length = 100)
    private String portCallNumber;

    @Column(name = "PRINCIPAL_NAME", length = 500)
    private String principalName;

    @Column(name = "NOMINATED_PARTY_TYPE", length = 100)
    private String nominatedPartyType;

    @Column(name = "NOMINATED_PARTY_POID")
    private Long nominatedPartyPoid;

    @Column(name = "DOCUMENT_SUBMITTED_DATE")
    private LocalDate documentSubmittedDate;

    @Column(name = "DOCUMENT_SUBMITTED_BY", length = 300)
    private String documentSubmittedBy;

    @Column(name = "DOCUMENT_SUBMITTED_STATUS", length = 300)
    private String documentSubmittedStatus = "NOT_SUBMITTED";

    @Column(name = "MULTIPLE_FDA", length = 1)
    private String multipleFda = "N";

    @Column(name = "SUB_CATEGORY", length = 100)
    private String subCategory;

    @Column(name = "MENAS_DUES", length = 1)
    private String menasDues = "N";

    @Column(name = "FDA_POID")
    private Long fdaPoid;

    @Column(name = "DOCUMENT_RECEIVED_DATE")
    private LocalDate documentReceivedDate;

    @Column(name = "DOCUMENT_RECEIVED_FROM", length = 300)
    private String documentReceivedFrom;

    @Column(name = "DOCUMENT_RECEIVED_STATUS", length = 300)
    private String documentReceivedStatus = "NOT_VERIFIED";

    @Column(name = "SUBMISSION_ACCEPTED_DATE")
    private LocalDate submissionAcceptedDate;

    @Column(name = "VERIFICATION_ACCEPTED_DATE")
    private LocalDate verificationAcceptedDate;

    @Column(name = "SUBMISSION_ACCEPTED_BY", length = 300)
    private String submissionAcceptedBy;

    @Column(name = "VERIFICATION_ACCEPTED_BY", length = 300)
    private String verificationAcceptedBy;

    @Column(name = "VESSEL_HANDLED_BY")
    private Long vesselHandledBy;

    @Column(name = "VESSEL_SAIL_DATE")
    private LocalDate vesselSailDate;

    @Column(name = "ACCTS_CORRECTION_REMARKS", length = 1000)
    private String acctsCorrectionRemarks;

    @Column(name = "ACCTS_RETURNED_DATE")
    private LocalDate acctsReturnedDate;

    @Column(name = "ACTUAL_SAIL_DATE")
    private LocalDate actualSailDate;

    @Column(name = "ACTUAL_ARRIVAL_DATE")
    private LocalDate actualArrivalDate;

}
