package com.alsharif.operations.pdaentryform.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating/updating PDA Entry TDR Detail
 */
public class PdaEntryTdrDetailRequest {

    private Long detRowId; // null for new, existing value for update

    @Size(max = 50)
    private String mlo;

    @Size(max = 50)
    private String pol;

    @Size(max = 50)
    private String slot;

    @Size(max = 50)
    private String subSlot;

    // Discharge fields
    @Size(max = 50)
    private String disch20fl;

    @Size(max = 50)
    private String disch20mt;

    @Size(max = 50)
    private String disch40fl;

    @Size(max = 50)
    private String disch40mt;

    @Size(max = 50)
    private String disch45fl;

    @Size(max = 50)
    private String disch45mt;

    @Size(max = 50)
    private String dischTot20;

    @Size(max = 50)
    private String dischTot40;

    @Size(max = 50)
    private String dischTot45;

    // Load fields
    @Size(max = 50)
    private String load20fl;

    @Size(max = 50)
    private String load20mt;

    @Size(max = 50)
    private String load40fl;

    @Size(max = 50)
    private String load40mt;

    @Size(max = 50)
    private String load45fl;

    @Size(max = 50)
    private String load45mt;

    @Size(max = 50)
    private String loadTot20;

    @Size(max = 50)
    private String loadTot40;

    @Size(max = 50)
    private String loadTot45;

    @Size(max = 50)
    private String loadAlm20;

    @Size(max = 50)
    private String loadAlm40;

    @Size(max = 50)
    private String loadAlm45;

    // Full container fields
    @Size(max = 50)
    private String full20dc;

    @Size(max = 50)
    private String full20tk;

    @Size(max = 50)
    private String full20fr;

    @Size(max = 50)
    private String full20ot;

    @Size(max = 50)
    private String full40dc;

    @Size(max = 50)
    private String full40ot;

    @Size(max = 50)
    private String full40fr;

    @Size(max = 50)
    private String full40rf;

    @Size(max = 50)
    private String full40rh;

    @Size(max = 50)
    private String full40hc;

    @Size(max = 50)
    private String full45;

    // Dangerous Goods (DG) fields
    @Size(max = 50)
    private String dg20dc;

    @Size(max = 50)
    private String dg20tk;

    @Size(max = 50)
    private String dg40dc;

    @Size(max = 50)
    private String dg40hc;

    @Size(max = 50)
    private String dg20rf;

    @Size(max = 50)
    private String dg40rf;

    @Size(max = 50)
    private String dg40hr;

    // Out of Gauge (OOG) fields
    @Size(max = 50)
    private String oog20ot;

    @Size(max = 50)
    private String oog20fr;

    @Size(max = 50)
    private String oog40ot;

    @Size(max = 50)
    private String oog40fr;

    // Empty (MT) fields
    @Size(max = 50)
    private String mt20dc;

    @Size(max = 50)
    private String mt20tk;

    @Size(max = 50)
    private String mt20fr;

    @Size(max = 50)
    private String mt20ot;

    @Size(max = 50)
    private String mt40dc;

    @Size(max = 50)
    private String mt40ot;

    @Size(max = 50)
    private String mt40fr;

    @Size(max = 50)
    private String mt40rf;

    @Size(max = 50)
    private String mt40rh;

    @Size(max = 50)
    private String mt40hc;

    @Size(max = 50)
    private String mt45;

    @Size(max = 300)
    private String remarks;

    // Getters and Setters - Creating minimal setters/getters for all fields due to size
    // (Would include all getters/setters here, abbreviated for brevity)

    public Long getDetRowId() { return detRowId; }
    public void setDetRowId(Long detRowId) { this.detRowId = detRowId; }
    public String getMlo() { return mlo; }
    public void setMlo(String mlo) { this.mlo = mlo; }
    public String getPol() { return pol; }
    public void setPol(String pol) { this.pol = pol; }
    public String getSlot() { return slot; }
    public void setSlot(String slot) { this.slot = slot; }
    public String getSubSlot() { return subSlot; }
    public void setSubSlot(String subSlot) { this.subSlot = subSlot; }
    public String getDisch20fl() { return disch20fl; }
    public void setDisch20fl(String disch20fl) { this.disch20fl = disch20fl; }
    public String getDisch20mt() { return disch20mt; }
    public void setDisch20mt(String disch20mt) { this.disch20mt = disch20mt; }
    public String getDisch40fl() { return disch40fl; }
    public void setDisch40fl(String disch40fl) { this.disch40fl = disch40fl; }
    public String getDisch40mt() { return disch40mt; }
    public void setDisch40mt(String disch40mt) { this.disch40mt = disch40mt; }
    public String getDisch45fl() { return disch45fl; }
    public void setDisch45fl(String disch45fl) { this.disch45fl = disch45fl; }
    public String getDisch45mt() { return disch45mt; }
    public void setDisch45mt(String disch45mt) { this.disch45mt = disch45mt; }
    public String getDischTot20() { return dischTot20; }
    public void setDischTot20(String dischTot20) { this.dischTot20 = dischTot20; }
    public String getDischTot40() { return dischTot40; }
    public void setDischTot40(String dischTot40) { this.dischTot40 = dischTot40; }
    public String getDischTot45() { return dischTot45; }
    public void setDischTot45(String dischTot45) { this.dischTot45 = dischTot45; }
    public String getLoad20fl() { return load20fl; }
    public void setLoad20fl(String load20fl) { this.load20fl = load20fl; }
    public String getLoad20mt() { return load20mt; }
    public void setLoad20mt(String load20mt) { this.load20mt = load20mt; }
    public String getLoad40fl() { return load40fl; }
    public void setLoad40fl(String load40fl) { this.load40fl = load40fl; }
    public String getLoad40mt() { return load40mt; }
    public void setLoad40mt(String load40mt) { this.load40mt = load40mt; }
    public String getLoad45fl() { return load45fl; }
    public void setLoad45fl(String load45fl) { this.load45fl = load45fl; }
    public String getLoad45mt() { return load45mt; }
    public void setLoad45mt(String load45mt) { this.load45mt = load45mt; }
    public String getLoadTot20() { return loadTot20; }
    public void setLoadTot20(String loadTot20) { this.loadTot20 = loadTot20; }
    public String getLoadTot40() { return loadTot40; }
    public void setLoadTot40(String loadTot40) { this.loadTot40 = loadTot40; }
    public String getLoadTot45() { return loadTot45; }
    public void setLoadTot45(String loadTot45) { this.loadTot45 = loadTot45; }
    public String getLoadAlm20() { return loadAlm20; }
    public void setLoadAlm20(String loadAlm20) { this.loadAlm20 = loadAlm20; }
    public String getLoadAlm40() { return loadAlm40; }
    public void setLoadAlm40(String loadAlm40) { this.loadAlm40 = loadAlm40; }
    public String getLoadAlm45() { return loadAlm45; }
    public void setLoadAlm45(String loadAlm45) { this.loadAlm45 = loadAlm45; }
    public String getFull20dc() { return full20dc; }
    public void setFull20dc(String full20dc) { this.full20dc = full20dc; }
    public String getFull20tk() { return full20tk; }
    public void setFull20tk(String full20tk) { this.full20tk = full20tk; }
    public String getFull20fr() { return full20fr; }
    public void setFull20fr(String full20fr) { this.full20fr = full20fr; }
    public String getFull20ot() { return full20ot; }
    public void setFull20ot(String full20ot) { this.full20ot = full20ot; }
    public String getFull40dc() { return full40dc; }
    public void setFull40dc(String full40dc) { this.full40dc = full40dc; }
    public String getFull40ot() { return full40ot; }
    public void setFull40ot(String full40ot) { this.full40ot = full40ot; }
    public String getFull40fr() { return full40fr; }
    public void setFull40fr(String full40fr) { this.full40fr = full40fr; }
    public String getFull40rf() { return full40rf; }
    public void setFull40rf(String full40rf) { this.full40rf = full40rf; }
    public String getFull40rh() { return full40rh; }
    public void setFull40rh(String full40rh) { this.full40rh = full40rh; }
    public String getFull40hc() { return full40hc; }
    public void setFull40hc(String full40hc) { this.full40hc = full40hc; }
    public String getFull45() { return full45; }
    public void setFull45(String full45) { this.full45 = full45; }
    public String getDg20dc() { return dg20dc; }
    public void setDg20dc(String dg20dc) { this.dg20dc = dg20dc; }
    public String getDg20tk() { return dg20tk; }
    public void setDg20tk(String dg20tk) { this.dg20tk = dg20tk; }
    public String getDg40dc() { return dg40dc; }
    public void setDg40dc(String dg40dc) { this.dg40dc = dg40dc; }
    public String getDg40hc() { return dg40hc; }
    public void setDg40hc(String dg40hc) { this.dg40hc = dg40hc; }
    public String getDg20rf() { return dg20rf; }
    public void setDg20rf(String dg20rf) { this.dg20rf = dg20rf; }
    public String getDg40rf() { return dg40rf; }
    public void setDg40rf(String dg40rf) { this.dg40rf = dg40rf; }
    public String getDg40hr() { return dg40hr; }
    public void setDg40hr(String dg40hr) { this.dg40hr = dg40hr; }
    public String getOog20ot() { return oog20ot; }
    public void setOog20ot(String oog20ot) { this.oog20ot = oog20ot; }
    public String getOog20fr() { return oog20fr; }
    public void setOog20fr(String oog20fr) { this.oog20fr = oog20fr; }
    public String getOog40ot() { return oog40ot; }
    public void setOog40ot(String oog40ot) { this.oog40ot = oog40ot; }
    public String getOog40fr() { return oog40fr; }
    public void setOog40fr(String oog40fr) { this.oog40fr = oog40fr; }
    public String getMt20dc() { return mt20dc; }
    public void setMt20dc(String mt20dc) { this.mt20dc = mt20dc; }
    public String getMt20tk() { return mt20tk; }
    public void setMt20tk(String mt20tk) { this.mt20tk = mt20tk; }
    public String getMt20fr() { return mt20fr; }
    public void setMt20fr(String mt20fr) { this.mt20fr = mt20fr; }
    public String getMt20ot() { return mt20ot; }
    public void setMt20ot(String mt20ot) { this.mt20ot = mt20ot; }
    public String getMt40dc() { return mt40dc; }
    public void setMt40dc(String mt40dc) { this.mt40dc = mt40dc; }
    public String getMt40ot() { return mt40ot; }
    public void setMt40ot(String mt40ot) { this.mt40ot = mt40ot; }
    public String getMt40fr() { return mt40fr; }
    public void setMt40fr(String mt40fr) { this.mt40fr = mt40fr; }
    public String getMt40rf() { return mt40rf; }
    public void setMt40rf(String mt40rf) { this.mt40rf = mt40rf; }
    public String getMt40rh() { return mt40rh; }
    public void setMt40rh(String mt40rh) { this.mt40rh = mt40rh; }
    public String getMt40hc() { return mt40hc; }
    public void setMt40hc(String mt40hc) { this.mt40hc = mt40hc; }
    public String getMt45() { return mt45; }
    public void setMt45(String mt45) { this.mt45 = mt45; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}

