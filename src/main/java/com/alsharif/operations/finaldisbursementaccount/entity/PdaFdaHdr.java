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
@Table(name = "PDA_FDA_HDR")
public class PdaFdaHdr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "DOC_REF", length = 25)
    private String docRef;

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

    @Column(name = "COMODITY_POID", length = 100)
    private String commodityPoid;

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
    private String vesselVerified;

    @Column(name = "VESSEL_VERIFIED_DATE")
    private LocalDate vesselVerifiedDate;

    @Column(name = "VESSEL_VERIFIED_BY", length = 30)
    private String vesselVerifiedBy;

    @Column(name = "URGENT_APPROVAL", length = 1)
    private String urgentApproval;

    @Column(name = "PRINCIPAL_APRVL_DAYS")
    private Long principalAprvlDays;

    @Column(name = "PRINCIPAL_APPROVED", length = 1)
    private String principalApproved;

    @Column(name = "PRINCIPAL_APPROVED_DATE")
    private LocalDate principalApprovedDate;

    @Column(name = "PRINCIPAL_APPROVED_BY", length = 30)
    private String principalApprovedBy;

    @Column(name = "REMINDER_MINUTES")
    private Long reminderMinutes;

    @Column(name = "CARGO_DETAILS", length = 100)
    private String cargoDetails;

    @Column(name = "STATUS", length = 30)
    private String status;

    @Column(name = "FDA_CLOSED_DATE")
    private LocalDate fdaClosedDate;

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
    private String deleted;

    @Column(name = "PDA_REF", length = 100)
    private String pdaRef;

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

    @Column(name = "IMO_NUMBER", length = 20)
    private String imoNumber;

    @Column(name = "NRT")
    private BigDecimal nrt;

    @Column(name = "NUMBER_OF_DAYS")
    private BigDecimal numberOfDays;

    @Column(name = "PORT_DESCRIPTION", length = 100)
    private String portDescription;

    @Column(name = "TERMS_POID")
    private Long termsPoid;

    @Column(name = "VESSEL_TYPE_POID", length = 30)
    private String vesselTypePoid;

    @Column(name = "LINE_POID")
    private Long linePoid;

    @Column(name = "PRINT_PRINCIPAL")
    private Long printPrincipal;

    @Column(name = "VOYAGE_NO", length = 30)
    private String voyageNo;

    @Column(name = "PROFIT_LOSS_AMOUNT")
    private BigDecimal profitLossAmount;

    @Column(name = "PROFIT_LOSS_PER", length = 50)
    private String profitLossPer;

    @Column(name = "FDA_CLOSING_BY", length = 100)
    private String fdaClosingBy;

    @Column(name = "GL_CLOSING_DATE")
    private LocalDate glClosingDate;

    @Column(name = "REF_TYPE", length = 100)
    private String refType;

    @Column(name = "CLOSED_REMARK", length = 500)
    private String closedRemark;

    @Column(name = "SUPPLEMENTARY", length = 1)
    private String supplementary;

    @Column(name = "SUPPLEMENTARY_FDA_POID")
    private Long supplementaryFdaPoid;

    @Column(name = "BUSINESS_REF_BY", length = 300)
    private String businessRefBy;

    @Column(name = "FDA_WITHOUT_CHARGES", length = 1)
    private String fdaWithoutCharges;

    @Column(name = "PRINT_BANK_POID")
    private Long printBankPoid;

    @Column(name = "PORT_CALL_NUMBER", length = 100)
    private String portCallNumber;

    @Column(name = "NOMINATED_PARTY_TYPE", length = 100)
    private String nominatedPartyType;

    @Column(name = "NOMINATED_PARTY_POID")
    private Long nominatedPartyPoid;

    @Column(name = "DOCUMENT_SUBMITTED_DATE")
    private LocalDate documentSubmittedDate;

    @Column(name = "DOCUMENT_SUBMITTED_BY", length = 300)
    private String documentSubmittedBy;

    @Column(name = "DOCUMENT_SUBMITTED_STATUS", length = 300)
    private String documentSubmittedStatus;

    @Column(name = "FDA_SUB_TYPE", length = 100)
    private String fdaSubType;

    @Column(name = "SUB_CATEGORY", length = 100)
    private String subCategory;

    @Column(name = "DOCUMENT_RECEIVED_DATE")
    private LocalDate documentReceivedDate;

    @Column(name = "DOCUMENT_RECEIVED_FROM", length = 300)
    private String documentReceivedFrom;

    @Column(name = "DOCUMENT_RECEIVED_STATUS", length = 300)
    private String documentReceivedStatus;

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

    @Column(name = "ACCOUNTS_VERIFIED", length = 1)
    private String accountsVerified;

    @Column(name = "OPS_CORRECTION_REMARKS", length = 1000)
    private String opsCorrectionRemarks;

    @Column(name = "OPS_RETURNED_DATE")
    private LocalDate opsReturnedDate;
}
