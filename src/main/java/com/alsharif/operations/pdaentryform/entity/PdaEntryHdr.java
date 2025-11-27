package com.alsharif.operations.pdaentryform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
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
    private BigDecimal groupPoid;

    @Column(name = "COMPANY_POID")
    private BigDecimal companyPoid;

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

    // Getters and Setters

    public Long getTransactionPoid() {
        return transactionPoid;
    }

    public void setTransactionPoid(Long transactionPoid) {
        this.transactionPoid = transactionPoid;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public BigDecimal getGroupPoid() {
        return groupPoid;
    }

    public void setGroupPoid(BigDecimal groupPoid) {
        this.groupPoid = groupPoid;
    }

    public BigDecimal getCompanyPoid() {
        return companyPoid;
    }

    public void setCompanyPoid(BigDecimal companyPoid) {
        this.companyPoid = companyPoid;
    }

    public String getDocRef() {
        return docRef;
    }

    public void setDocRef(String docRef) {
        this.docRef = docRef;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public BigDecimal getPrincipalPoid() {
        return principalPoid;
    }

    public void setPrincipalPoid(BigDecimal principalPoid) {
        this.principalPoid = principalPoid;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public String getPrincipalContact() {
        return principalContact;
    }

    public void setPrincipalContact(String principalContact) {
        this.principalContact = principalContact;
    }

    public BigDecimal getVoyagePoid() {
        return voyagePoid;
    }

    public void setVoyagePoid(BigDecimal voyagePoid) {
        this.voyagePoid = voyagePoid;
    }

    public String getVoyageNo() {
        return voyageNo;
    }

    public void setVoyageNo(String voyageNo) {
        this.voyageNo = voyageNo;
    }

    public BigDecimal getVesselPoid() {
        return vesselPoid;
    }

    public void setVesselPoid(BigDecimal vesselPoid) {
        this.vesselPoid = vesselPoid;
    }

    public BigDecimal getVesselTypePoid() {
        return vesselTypePoid;
    }

    public void setVesselTypePoid(BigDecimal vesselTypePoid) {
        this.vesselTypePoid = vesselTypePoid;
    }

    public BigDecimal getGrt() {
        return grt;
    }

    public void setGrt(BigDecimal grt) {
        this.grt = grt;
    }

    public BigDecimal getNrt() {
        return nrt;
    }

    public void setNrt(BigDecimal nrt) {
        this.nrt = nrt;
    }

    public BigDecimal getDwt() {
        return dwt;
    }

    public void setDwt(BigDecimal dwt) {
        this.dwt = dwt;
    }

    public String getImoNumber() {
        return imoNumber;
    }

    public void setImoNumber(String imoNumber) {
        this.imoNumber = imoNumber;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public LocalDate getSailDate() {
        return sailDate;
    }

    public void setSailDate(LocalDate sailDate) {
        this.sailDate = sailDate;
    }

    public LocalDate getActualArrivalDate() {
        return actualArrivalDate;
    }

    public void setActualArrivalDate(LocalDate actualArrivalDate) {
        this.actualArrivalDate = actualArrivalDate;
    }

    public LocalDate getActualSailDate() {
        return actualSailDate;
    }

    public void setActualSailDate(LocalDate actualSailDate) {
        this.actualSailDate = actualSailDate;
    }

    public LocalDate getVesselSailDate() {
        return vesselSailDate;
    }

    public void setVesselSailDate(LocalDate vesselSailDate) {
        this.vesselSailDate = vesselSailDate;
    }

    public BigDecimal getPortPoid() {
        return portPoid;
    }

    public void setPortPoid(BigDecimal portPoid) {
        this.portPoid = portPoid;
    }

    public String getPortDescription() {
        return portDescription;
    }

    public void setPortDescription(String portDescription) {
        this.portDescription = portDescription;
    }

    public BigDecimal getLinePoid() {
        return linePoid;
    }

    public void setLinePoid(BigDecimal linePoid) {
        this.linePoid = linePoid;
    }

    public String getComodityPoid() {
        return comodityPoid;
    }

    public void setComodityPoid(String comodityPoid) {
        this.comodityPoid = comodityPoid;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getHarbourCallType() {
        return harbourCallType;
    }

    public void setHarbourCallType(String harbourCallType) {
        this.harbourCallType = harbourCallType;
    }

    public BigDecimal getImportQty() {
        return importQty;
    }

    public void setImportQty(BigDecimal importQty) {
        this.importQty = importQty;
    }

    public BigDecimal getExportQty() {
        return exportQty;
    }

    public void setExportQty(BigDecimal exportQty) {
        this.exportQty = exportQty;
    }

    public BigDecimal getTranshipmentQty() {
        return transhipmentQty;
    }

    public void setTranshipmentQty(BigDecimal transhipmentQty) {
        this.transhipmentQty = transhipmentQty;
    }

    public BigDecimal getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(BigDecimal totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(BigDecimal numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getCurrencyRate() {
        return currencyRate;
    }

    public void setCurrencyRate(BigDecimal currencyRate) {
        this.currencyRate = currencyRate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getCostCentrePoid() {
        return costCentrePoid;
    }

    public void setCostCentrePoid(BigDecimal costCentrePoid) {
        this.costCentrePoid = costCentrePoid;
    }

    public BigDecimal getSalesmanPoid() {
        return salesmanPoid;
    }

    public void setSalesmanPoid(BigDecimal salesmanPoid) {
        this.salesmanPoid = salesmanPoid;
    }

    public BigDecimal getTermsPoid() {
        return termsPoid;
    }

    public void setTermsPoid(BigDecimal termsPoid) {
        this.termsPoid = termsPoid;
    }

    public BigDecimal getAddressPoid() {
        return addressPoid;
    }

    public void setAddressPoid(BigDecimal addressPoid) {
        this.addressPoid = addressPoid;
    }

    public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCargoDetails() {
        return cargoDetails;
    }

    public void setCargoDetails(String cargoDetails) {
        this.cargoDetails = cargoDetails;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getVesselVerified() {
        return vesselVerified;
    }

    public void setVesselVerified(String vesselVerified) {
        this.vesselVerified = vesselVerified;
    }

    public LocalDate getVesselVerifiedDate() {
        return vesselVerifiedDate;
    }

    public void setVesselVerifiedDate(LocalDate vesselVerifiedDate) {
        this.vesselVerifiedDate = vesselVerifiedDate;
    }

    public String getVesselVerifiedBy() {
        return vesselVerifiedBy;
    }

    public void setVesselVerifiedBy(String vesselVerifiedBy) {
        this.vesselVerifiedBy = vesselVerifiedBy;
    }

    public BigDecimal getVesselHandledBy() {
        return vesselHandledBy;
    }

    public void setVesselHandledBy(BigDecimal vesselHandledBy) {
        this.vesselHandledBy = vesselHandledBy;
    }

    public String getUrgentApproval() {
        return urgentApproval;
    }

    public void setUrgentApproval(String urgentApproval) {
        this.urgentApproval = urgentApproval;
    }

    public String getPrincipalApproved() {
        return principalApproved;
    }

    public void setPrincipalApproved(String principalApproved) {
        this.principalApproved = principalApproved;
    }

    public LocalDate getPrincipalApprovedDate() {
        return principalApprovedDate;
    }

    public void setPrincipalApprovedDate(LocalDate principalApprovedDate) {
        this.principalApprovedDate = principalApprovedDate;
    }

    public String getPrincipalApprovedBy() {
        return principalApprovedBy;
    }

    public void setPrincipalApprovedBy(String principalApprovedBy) {
        this.principalApprovedBy = principalApprovedBy;
    }

    public BigDecimal getPrincipalAprvlDays() {
        return principalAprvlDays;
    }

    public void setPrincipalAprvlDays(BigDecimal principalAprvlDays) {
        this.principalAprvlDays = principalAprvlDays;
    }

    public BigDecimal getReminderMinutes() {
        return reminderMinutes;
    }

    public void setReminderMinutes(BigDecimal reminderMinutes) {
        this.reminderMinutes = reminderMinutes;
    }

    public BigDecimal getPrintPrincipal() {
        return printPrincipal;
    }

    public void setPrintPrincipal(BigDecimal printPrincipal) {
        this.printPrincipal = printPrincipal;
    }

    public String getFdaRef() {
        return fdaRef;
    }

    public void setFdaRef(String fdaRef) {
        this.fdaRef = fdaRef;
    }

    public BigDecimal getFdaPoid() {
        return fdaPoid;
    }

    public void setFdaPoid(BigDecimal fdaPoid) {
        this.fdaPoid = fdaPoid;
    }

    public String getMultipleFda() {
        return multipleFda;
    }

    public void setMultipleFda(String multipleFda) {
        this.multipleFda = multipleFda;
    }

    public String getNominatedPartyType() {
        return nominatedPartyType;
    }

    public void setNominatedPartyType(String nominatedPartyType) {
        this.nominatedPartyType = nominatedPartyType;
    }

    public BigDecimal getNominatedPartyPoid() {
        return nominatedPartyPoid;
    }

    public void setNominatedPartyPoid(BigDecimal nominatedPartyPoid) {
        this.nominatedPartyPoid = nominatedPartyPoid;
    }

    public BigDecimal getBankPoid() {
        return bankPoid;
    }

    public void setBankPoid(BigDecimal bankPoid) {
        this.bankPoid = bankPoid;
    }

    public String getBusinessRefBy() {
        return businessRefBy;
    }

    public void setBusinessRefBy(String businessRefBy) {
        this.businessRefBy = businessRefBy;
    }

    public String getPmiDocument() {
        return pmiDocument;
    }

    public void setPmiDocument(String pmiDocument) {
        this.pmiDocument = pmiDocument;
    }

    public String getCancelRemark() {
        return cancelRemark;
    }

    public void setCancelRemark(String cancelRemark) {
        this.cancelRemark = cancelRemark;
    }

    public String getOldPortCode() {
        return oldPortCode;
    }

    public void setOldPortCode(String oldPortCode) {
        this.oldPortCode = oldPortCode;
    }

    public String getOldVesselCode() {
        return oldVesselCode;
    }

    public void setOldVesselCode(String oldVesselCode) {
        this.oldVesselCode = oldVesselCode;
    }

    public String getOldPrincipalCode() {
        return oldPrincipalCode;
    }

    public void setOldPrincipalCode(String oldPrincipalCode) {
        this.oldPrincipalCode = oldPrincipalCode;
    }

    public String getOldVoyageJob() {
        return oldVoyageJob;
    }

    public void setOldVoyageJob(String oldVoyageJob) {
        this.oldVoyageJob = oldVoyageJob;
    }

    public String getMenasDues() {
        return menasDues;
    }

    public void setMenasDues(String menasDues) {
        this.menasDues = menasDues;
    }

    public LocalDate getDocumentSubmittedDate() {
        return documentSubmittedDate;
    }

    public void setDocumentSubmittedDate(LocalDate documentSubmittedDate) {
        this.documentSubmittedDate = documentSubmittedDate;
    }

    public String getDocumentSubmittedBy() {
        return documentSubmittedBy;
    }

    public void setDocumentSubmittedBy(String documentSubmittedBy) {
        this.documentSubmittedBy = documentSubmittedBy;
    }

    public String getDocumentSubmittedStatus() {
        return documentSubmittedStatus;
    }

    public void setDocumentSubmittedStatus(String documentSubmittedStatus) {
        this.documentSubmittedStatus = documentSubmittedStatus;
    }

    public LocalDate getDocumentReceivedDate() {
        return documentReceivedDate;
    }

    public void setDocumentReceivedDate(LocalDate documentReceivedDate) {
        this.documentReceivedDate = documentReceivedDate;
    }

    public String getDocumentReceivedFrom() {
        return documentReceivedFrom;
    }

    public void setDocumentReceivedFrom(String documentReceivedFrom) {
        this.documentReceivedFrom = documentReceivedFrom;
    }

    public String getDocumentReceivedStatus() {
        return documentReceivedStatus;
    }

    public void setDocumentReceivedStatus(String documentReceivedStatus) {
        this.documentReceivedStatus = documentReceivedStatus;
    }

    public LocalDate getSubmissionAcceptedDate() {
        return submissionAcceptedDate;
    }

    public void setSubmissionAcceptedDate(LocalDate submissionAcceptedDate) {
        this.submissionAcceptedDate = submissionAcceptedDate;
    }

    public String getSubmissionAcceptedBy() {
        return submissionAcceptedBy;
    }

    public void setSubmissionAcceptedBy(String submissionAcceptedBy) {
        this.submissionAcceptedBy = submissionAcceptedBy;
    }

    public LocalDate getVerificationAcceptedDate() {
        return verificationAcceptedDate;
    }

    public void setVerificationAcceptedDate(LocalDate verificationAcceptedDate) {
        this.verificationAcceptedDate = verificationAcceptedDate;
    }

    public String getVerificationAcceptedBy() {
        return verificationAcceptedBy;
    }

    public void setVerificationAcceptedBy(String verificationAcceptedBy) {
        this.verificationAcceptedBy = verificationAcceptedBy;
    }

    public String getAcctsCorrectionRemarks() {
        return acctsCorrectionRemarks;
    }

    public void setAcctsCorrectionRemarks(String acctsCorrectionRemarks) {
        this.acctsCorrectionRemarks = acctsCorrectionRemarks;
    }

    public LocalDate getAcctsReturnedDate() {
        return acctsReturnedDate;
    }

    public void setAcctsReturnedDate(LocalDate acctsReturnedDate) {
        this.acctsReturnedDate = acctsReturnedDate;
    }

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public List<PdaEntryDtl> getChargeDetails() {
        return chargeDetails;
    }

    public void setChargeDetails(List<PdaEntryDtl> chargeDetails) {
        this.chargeDetails = chargeDetails;
    }

    public List<PdaEntryVehicleDtl> getVehicleDetails() {
        return vehicleDetails;
    }

    public void setVehicleDetails(List<PdaEntryVehicleDtl> vehicleDetails) {
        this.vehicleDetails = vehicleDetails;
    }

    public List<PdaEntryTdrDetail> getTdrDetails() {
        return tdrDetails;
    }

    public void setTdrDetails(List<PdaEntryTdrDetail> tdrDetails) {
        this.tdrDetails = tdrDetails;
    }

    public List<PdaEntryAcknowledgmentDtl> getAcknowledgmentDetails() {
        return acknowledgmentDetails;
    }

    public void setAcknowledgmentDetails(List<PdaEntryAcknowledgmentDtl> acknowledgmentDetails) {
        this.acknowledgmentDetails = acknowledgmentDetails;
    }

    public void setActive(String y) {
    }
}

