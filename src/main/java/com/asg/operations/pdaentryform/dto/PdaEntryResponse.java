package com.asg.operations.pdaentryform.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for PDA Entry Form operations
 */
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.List;


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
    private String principalName;
    private String principalContact;
    private BigDecimal voyagePoid;
    private String voyageNo;
    private BigDecimal vesselPoid;
    private BigDecimal vesselTypePoid;
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
    private String portDescription;
    private BigDecimal linePoid;
    private String comodityPoid;
    private String operationType;
    private String harbourCallType;
    private BigDecimal importQty;
    private BigDecimal exportQty;
    private BigDecimal transhipmentQty;
    private BigDecimal totalQuantity;
    private String unit;
    private BigDecimal numberOfDays;
    private String currencyCode;
    private BigDecimal currencyRate;
    private BigDecimal totalAmount;
    private BigDecimal costCentrePoid;
    private BigDecimal salesmanPoid;
    private BigDecimal termsPoid;
    private BigDecimal addressPoid;
    private String refType;
    private String subCategory;
    private String status;
    private String cargoDetails;
    private String remarks;
    private String vesselVerified;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate vesselVerifiedDate;

    private String vesselVerifiedBy;
    private BigDecimal vesselHandledBy;
    private String urgentApproval;
    private String principalApproved;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate principalApprovedDate;

    private String principalApprovedBy;
    private BigDecimal principalAprvlDays;
    private BigDecimal reminderMinutes;
    private BigDecimal printPrincipal;
    private String fdaRef;
    private BigDecimal fdaPoid;
    private String multipleFda;
    private String nominatedPartyType;
    private BigDecimal nominatedPartyPoid;
    private BigDecimal bankPoid;
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

    // Getters and Setters

    public Long getTransactionPoid() {
        return transactionPoid;
    }

    public void setTransactionPoid(Long transactionPoid) {
        this.transactionPoid = transactionPoid;
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

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
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

    public List<PdaEntryChargeDetailResponse> getChargeDetails() {
        return chargeDetails;
    }

    public void setChargeDetails(List<PdaEntryChargeDetailResponse> chargeDetails) {
        this.chargeDetails = chargeDetails;
    }

    public List<PdaEntryVehicleDetailResponse> getVehicleDetails() {
        return vehicleDetails;
    }

    public void setVehicleDetails(List<PdaEntryVehicleDetailResponse> vehicleDetails) {
        this.vehicleDetails = vehicleDetails;
    }

    public List<PdaEntryTdrDetailResponse> getTdrDetails() {
        return tdrDetails;
    }

    public void setTdrDetails(List<PdaEntryTdrDetailResponse> tdrDetails) {
        this.tdrDetails = tdrDetails;
    }

    public List<PdaEntryAcknowledgmentDetailResponse> getAcknowledgmentDetails() {
        return acknowledgmentDetails;
    }

    public void setAcknowledgmentDetails(List<PdaEntryAcknowledgmentDetailResponse> acknowledgmentDetails) {
        this.acknowledgmentDetails = acknowledgmentDetails;
    }
}

