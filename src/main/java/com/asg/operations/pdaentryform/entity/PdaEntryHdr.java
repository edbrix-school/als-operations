package com.asg.operations.pdaentryform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "PDA_ENTRY_HDR")
public class PdaEntryHdr {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pda_entry_hdr_seq")
    @SequenceGenerator(name = "pda_entry_hdr_seq", sequenceName = "PDA_ENTRY_HDR_SEQ", allocationSize = 1)
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @Column(name = "TRANSACTION_DATE", nullable = false)
    @NotNull
    private LocalDate transactionDate;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "DOC_REF", unique = true, length = 25)
    @Size(max = 25)
    private String docRef;

    @Column(name = "TRANSACTION_REF", unique = true, length = 30)
    @Size(max = 30)
    private String transactionRef;

    @Column(name = "PRINCIPAL_POID")
    @NotNull
    private BigDecimal principalPoid;

    @Column(name = "PRINCIPAL_NAME", length = 500)
    @Size(max = 500)
    private String principalName;

    @Column(name = "PRINCIPAL_CONTACT", length = 50)
    @Size(max = 50)
    private String principalContact;

    @Column(name = "VOYAGE_POID")
    private BigDecimal voyagePoid;

    @Column(name = "VOYAGE_NO", length = 30)
    @Size(max = 30)
    private String voyageNo;

    @Column(name = "VESSEL_POID")
    private BigDecimal vesselPoid;

    @Column(name = "VESSEL_TYPE_POID")
    private BigDecimal vesselTypePoid;

    @Column(name = "GRT")
    private BigDecimal grt;

    @Column(name = "NRT")
    private BigDecimal nrt;

    @Column(name = "DWT")
    private BigDecimal dwt;

    @Column(name = "IMO_NUMBER", length = 20)
    @Size(max = 20)
    private String imoNumber;

    @Column(name = "ARRIVAL_DATE")
    private LocalDate arrivalDate;

    @Column(name = "SAIL_DATE")
    private LocalDate sailDate;

    @Column(name = "ACTUAL_ARRIVAL_DATE")
    private LocalDate actualArrivalDate;

    @Column(name = "ACTUAL_SAIL_DATE")
    private LocalDate actualSailDate;

    @Column(name = "VESSEL_SAIL_DATE")
    private LocalDate vesselSailDate;

    @Column(name = "PORT_POID")
    private BigDecimal portPoid;

    @Column(name = "PORT_DESCRIPTION", length = 100)
    @Size(max = 100)
    private String portDescription;

    @Column(name = "LINE_POID")
    private BigDecimal linePoid;

    @Column(name = "COMODITY_POID", length = 50)
    @Size(max = 50)
    private String comodityPoid;

    @Column(name = "OPERATION_TYPE", length = 30)
    @Size(max = 30)
    private String operationType;

    @Column(name = "HARBOUR_CALL_TYPE", length = 20)
    @Size(max = 20)
    private String harbourCallType;

    @Column(name = "IMPORT_QTY")
    private BigDecimal importQty;

    @Column(name = "EXPORT_QTY")
    private BigDecimal exportQty;

    @Column(name = "TRANSHIPMENT_QTY")
    private BigDecimal transhipmentQty;

    @Column(name = "TOTAL_QUANTITY")
    private BigDecimal totalQuantity;

    @Column(name = "UNIT", length = 20)
    @Size(max = 20)
    private String unit;

    @Column(name = "NUMBER_OF_DAYS")
    private BigDecimal numberOfDays;

    @Column(name = "CURRENCY_CODE", length = 20)
    @Size(max = 20)
    private String currencyCode;

    @Column(name = "CURRENCY_RATE")
    private BigDecimal currencyRate;

    @Column(name = "TOTAL_AMOUNT")
    private BigDecimal totalAmount;

    @Column(name = "COST_CENTRE_POID")
    private BigDecimal costCentrePoid;

    @Column(name = "SALESMAN_POID")
    private BigDecimal salesmanPoid;

    @Column(name = "TERMS_POID")
    private BigDecimal termsPoid;

    @Column(name = "ADDRESS_POID")
    private BigDecimal addressPoid;

    @Column(name = "REF_TYPE", nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    private String refType;

    @Column(name = "SUB_CATEGORY", length = 100)
    @Size(max = 100)
    private String subCategory;

    @Column(name = "STATUS", length = 30)
    @Size(max = 30)
    private String status;

    @Column(name = "CARGO_DETAILS", length = 100)
    @Size(max = 100)
    private String cargoDetails;

    @Column(name = "REMARKS", length = 2000)
    @Size(max = 2000)
    private String remarks;

    @Column(name = "VESSEL_VERIFIED", length = 1)
    @Size(max = 1)
    private String vesselVerified;

    @Column(name = "VESSEL_VERIFIED_DATE")
    private LocalDate vesselVerifiedDate;

    @Column(name = "VESSEL_VERIFIED_BY", length = 30)
    @Size(max = 30)
    private String vesselVerifiedBy;

    @Column(name = "VESSEL_HANDLED_BY")
    private BigDecimal vesselHandledBy;

    @Column(name = "URGENT_APPROVAL", length = 1)
    @Size(max = 1)
    private String urgentApproval;

    @Column(name = "PRINCIPAL_APPROVED", length = 1)
    @Size(max = 1)
    private String principalApproved;

    @Column(name = "PRINCIPAL_APPROVED_DATE")
    private LocalDate principalApprovedDate;

    @Column(name = "PRINCIPAL_APPROVED_BY", length = 30)
    @Size(max = 30)
    private String principalApprovedBy;

    @Column(name = "PRINCIPAL_APRVL_DAYS")
    private BigDecimal principalAprvlDays;

    @Column(name = "REMINDER_MINUTES")
    private BigDecimal reminderMinutes;

    @Column(name = "PRINT_PRINCIPAL")
    private BigDecimal printPrincipal;

    @Column(name = "FDA_REF", length = 100)
    @Size(max = 100)
    private String fdaRef;

    @Column(name = "FDA_POID")
    private BigDecimal fdaPoid;

    @Column(name = "MULTIPLE_FDA", length = 1)
    @Size(max = 1)
    private String multipleFda;

    @Column(name = "NOMINATED_PARTY_TYPE", length = 100)
    @Size(max = 100)
    private String nominatedPartyType;

    @Column(name = "NOMINATED_PARTY_POID")
    private BigDecimal nominatedPartyPoid;

    @Column(name = "BANK_POID")
    private BigDecimal bankPoid;

    @Column(name = "BUSINESS_REF_BY", length = 300)
    @Size(max = 300)
    private String businessRefBy;

    @Column(name = "PMI_DOCUMENT", length = 1)
    @Size(max = 1)
    private String pmiDocument;

    @Column(name = "CANCEL_REMARK", length = 500)
    @Size(max = 500)
    private String cancelRemark;

    @Column(name = "OLD_PORT_CODE", length = 20)
    @Size(max = 20)
    private String oldPortCode;

    @Column(name = "OLD_VESSEL_CODE", length = 20)
    @Size(max = 20)
    private String oldVesselCode;

    @Column(name = "OLD_PRINCIPAL_CODE", length = 20)
    @Size(max = 20)
    private String oldPrincipalCode;

    @Column(name = "OLD_VOYAGE_JOB", length = 20)
    @Size(max = 20)
    private String oldVoyageJob;

    @Column(name = "MENAS_DUES", length = 1)
    @Size(max = 1)
    private String menasDues;

    @Column(name = "DOCUMENT_SUBMITTED_DATE")
    private LocalDate documentSubmittedDate;

    @Column(name = "DOCUMENT_SUBMITTED_BY", length = 300)
    @Size(max = 300)
    private String documentSubmittedBy;

    @Column(name = "DOCUMENT_SUBMITTED_STATUS", length = 300)
    @Size(max = 300)
    private String documentSubmittedStatus;

    @Column(name = "DOCUMENT_RECEIVED_DATE")
    private LocalDate documentReceivedDate;

    @Column(name = "DOCUMENT_RECEIVED_FROM", length = 300)
    @Size(max = 300)
    private String documentReceivedFrom;

    @Column(name = "DOCUMENT_RECEIVED_STATUS", length = 300)
    @Size(max = 300)
    private String documentReceivedStatus;

    @Column(name = "SUBMISSION_ACCEPTED_DATE")
    private LocalDate submissionAcceptedDate;

    @Column(name = "SUBMISSION_ACCEPTED_BY", length = 300)
    @Size(max = 300)
    private String submissionAcceptedBy;

    @Column(name = "VERIFICATION_ACCEPTED_DATE")
    private LocalDate verificationAcceptedDate;

    @Column(name = "VERIFICATION_ACCEPTED_BY", length = 300)
    @Size(max = 300)
    private String verificationAcceptedBy;

    @Column(name = "ACCTS_CORRECTION_REMARKS", length = 1000)
    @Size(max = 1000)
    private String acctsCorrectionRemarks;

    @Column(name = "ACCTS_RETURNED_DATE")
    private LocalDate acctsReturnedDate;

    @Column(name = "DELETED", length = 1)
    @Size(max = 1)
    private String deleted;

    @Column(name = "CREATED_BY", length = 20)
    @Size(max = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    @Size(max = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @OneToMany(mappedBy = "entryHdr", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PdaEntryDtl> chargeDetails;

    @OneToMany(mappedBy = "entryHdr", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PdaEntryVehicleDtl> vehicleDetails;

    @OneToMany(mappedBy = "entryHdr", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PdaEntryTdrDetail> tdrDetails;

    @OneToMany(mappedBy = "entryHdr", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PdaEntryAcknowledgmentDtl> acknowledgmentDetails;

}

