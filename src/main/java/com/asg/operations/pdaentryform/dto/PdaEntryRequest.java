package com.asg.operations.pdaentryform.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;

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

    // Getters and Setters

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
}

