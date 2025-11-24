package com.alsharif.operations.pdaratetypemaster.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class PdaRateTypeResponseDTO {

    private Long rateTypeId;
    private String rateTypeCode;
    private String rateTypeName;
    private String rateTypeName2;
    private String rateTypeFormula;
    private String defQty;
    private BigDecimal defDays;
    private BigInteger seqNo;
    private String active;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;

    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime modifiedDate;

    private String modifiedBy;

    // Getters and Setters
    public Long getRateTypeId() {
        return rateTypeId;
    }

    public void setRateTypeId(Long rateTypeId) {
        this.rateTypeId = rateTypeId;
    }

    public String getRateTypeCode() {
        return rateTypeCode;
    }

    public void setRateTypeCode(String rateTypeCode) {
        this.rateTypeCode = rateTypeCode;
    }

    public String getRateTypeName() {
        return rateTypeName;
    }

    public void setRateTypeName(String rateTypeName) {
        this.rateTypeName = rateTypeName;
    }

    public String getRateTypeName2() {
        return rateTypeName2;
    }

    public void setRateTypeName2(String rateTypeName2) {
        this.rateTypeName2 = rateTypeName2;
    }

    public String getRateTypeFormula() {
        return rateTypeFormula;
    }

    public void setRateTypeFormula(String rateTypeFormula) {
        this.rateTypeFormula = rateTypeFormula;
    }

    public String getDefQty() {
        return defQty;
    }

    public void setDefQty(String defQty) {
        this.defQty = defQty;
    }

    public BigDecimal getDefDays() {
        return defDays;
    }

    public void setDefDays(BigDecimal defDays) {
        this.defDays = defDays;
    }

    public BigInteger getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(BigInteger seqNo) {
        this.seqNo = seqNo;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
}