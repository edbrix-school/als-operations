package com.asg.operations.pdaentryform.dto;

import java.math.BigDecimal;

/**
 * Response DTO for vessel details (auto-populated from LOV change)
 */
public class VesselDetailsResponse {

    private BigDecimal vesselTypePoid;
    private String imoNumber;
    private BigDecimal grt;
    private BigDecimal nrt;
    private BigDecimal dwt;

    // Getters and Setters

    public BigDecimal getVesselTypePoid() {
        return vesselTypePoid;
    }

    public void setVesselTypePoid(BigDecimal vesselTypePoid) {
        this.vesselTypePoid = vesselTypePoid;
    }

    public String getImoNumber() {
        return imoNumber;
    }

    public void setImoNumber(String imoNumber) {
        this.imoNumber = imoNumber;
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
}

