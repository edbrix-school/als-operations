package com.alsharif.operations.pdaentryform.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Request DTO for creating/updating PDA Entry Charge Detail
 */
public class PdaEntryChargeDetailRequest {

    private Long detRowId; // null for new, existing value for update

    @NotNull(message = "Charge POID is mandatory")
    private BigDecimal chargePoid;

    private BigDecimal rateTypePoid;

    private BigDecimal principalPoid;

    @Size(max = 20)
    private String currencyCode;

    private BigDecimal currencyRate;

    @NotNull(message = "Quantity is mandatory")
    private BigDecimal qty;

    @NotNull(message = "Days is mandatory")
    private BigDecimal days;

    @NotNull(message = "PDA Rate is mandatory")
    private BigDecimal pdaRate;

    private BigDecimal taxPoid;

    private BigDecimal taxPercentage;

    private BigDecimal taxAmount;

    private BigDecimal amount;

    private BigDecimal fdaAmount;

    @Size(max = 100)
    private String fdaDocRef;

    private BigDecimal fdaPoid;

    @Size(max = 100)
    private String fdaCreationType;

    @Size(max = 20)
    private String dataSource;

    @Size(max = 100)
    private String detailFrom;

    @Size(max = 1)
    private String manual;

    private Integer seqno;

    @Size(max = 500)
    private String remarks;

    // Getters and Setters

    public Long getDetRowId() {
        return detRowId;
    }

    public void setDetRowId(Long detRowId) {
        this.detRowId = detRowId;
    }

    public BigDecimal getChargePoid() {
        return chargePoid;
    }

    public void setChargePoid(BigDecimal chargePoid) {
        this.chargePoid = chargePoid;
    }

    public BigDecimal getRateTypePoid() {
        return rateTypePoid;
    }

    public void setRateTypePoid(BigDecimal rateTypePoid) {
        this.rateTypePoid = rateTypePoid;
    }

    public BigDecimal getPrincipalPoid() {
        return principalPoid;
    }

    public void setPrincipalPoid(BigDecimal principalPoid) {
        this.principalPoid = principalPoid;
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

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public BigDecimal getDays() {
        return days;
    }

    public void setDays(BigDecimal days) {
        this.days = days;
    }

    public BigDecimal getPdaRate() {
        return pdaRate;
    }

    public void setPdaRate(BigDecimal pdaRate) {
        this.pdaRate = pdaRate;
    }

    public BigDecimal getTaxPoid() {
        return taxPoid;
    }

    public void setTaxPoid(BigDecimal taxPoid) {
        this.taxPoid = taxPoid;
    }

    public BigDecimal getTaxPercentage() {
        return taxPercentage;
    }

    public void setTaxPercentage(BigDecimal taxPercentage) {
        this.taxPercentage = taxPercentage;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getFdaAmount() {
        return fdaAmount;
    }

    public void setFdaAmount(BigDecimal fdaAmount) {
        this.fdaAmount = fdaAmount;
    }

    public String getFdaDocRef() {
        return fdaDocRef;
    }

    public void setFdaDocRef(String fdaDocRef) {
        this.fdaDocRef = fdaDocRef;
    }

    public BigDecimal getFdaPoid() {
        return fdaPoid;
    }

    public void setFdaPoid(BigDecimal fdaPoid) {
        this.fdaPoid = fdaPoid;
    }

    public String getFdaCreationType() {
        return fdaCreationType;
    }

    public void setFdaCreationType(String fdaCreationType) {
        this.fdaCreationType = fdaCreationType;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getDetailFrom() {
        return detailFrom;
    }

    public void setDetailFrom(String detailFrom) {
        this.detailFrom = detailFrom;
    }

    public String getManual() {
        return manual;
    }

    public void setManual(String manual) {
        this.manual = manual;
    }

    public Integer getSeqno() {
        return seqno;
    }

    public void setSeqno(Integer seqno) {
        this.seqno = seqno;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}

