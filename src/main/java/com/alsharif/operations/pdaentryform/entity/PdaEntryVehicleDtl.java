package com.alsharif.operations.pdaentryform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "PDA_ENTRY_VEHICLE_DTL")
@IdClass(PdaEntryVehicleDtlId.class)
public class PdaEntryVehicleDtl {

    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    @NotNull
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    @NotNull
    private Long detRowId;

    @Column(name = "VESSEL_NAME", length = 100)
    @Size(max = 100)
    private String vesselName;

    @Column(name = "VOYAGE_REF", length = 50)
    @Size(max = 50)
    private String voyageRef;

    @Column(name = "IN_OUT_MODE", length = 5)
    @Size(max = 5)
    private String inOutMode;

    @Column(name = "VEHICLE_MODEL", length = 100)
    @Size(max = 100)
    private String vehicleModel;

    @Column(name = "VIN_NUMBER", length = 100)
    @Size(max = 100)
    private String vinNumber;

    @Column(name = "SCAN_DATE")
    private LocalDateTime scanDate;

    @Column(name = "DAMAGE", length = 5)
    @Size(max = 5)
    private String damage;

    @Column(name = "STATUS", length = 50)
    @Size(max = 50)
    private String status;

    @Column(name = "PUBLISH_FOR_IMPORT", length = 1)
    @Size(max = 1)
    private String publishForImport;

    @Column(name = "REMARKS", length = 500)
    @Size(max = 500)
    private String remarks;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSACTION_POID", insertable = false, updatable = false)
    private PdaEntryHdr entryHdr;

    // Getters and Setters

    public Long getTransactionPoid() {
        return transactionPoid;
    }

    public void setTransactionPoid(Long transactionPoid) {
        this.transactionPoid = transactionPoid;
    }

    public Long getDetRowId() {
        return detRowId;
    }

    public void setDetRowId(Long detRowId) {
        this.detRowId = detRowId;
    }

    public String getVesselName() {
        return vesselName;
    }

    public void setVesselName(String vesselName) {
        this.vesselName = vesselName;
    }

    public String getVoyageRef() {
        return voyageRef;
    }

    public void setVoyageRef(String voyageRef) {
        this.voyageRef = voyageRef;
    }

    public String getInOutMode() {
        return inOutMode;
    }

    public void setInOutMode(String inOutMode) {
        this.inOutMode = inOutMode;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getVinNumber() {
        return vinNumber;
    }

    public void setVinNumber(String vinNumber) {
        this.vinNumber = vinNumber;
    }

    public LocalDateTime getScanDate() {
        return scanDate;
    }

    public void setScanDate(LocalDateTime scanDate) {
        this.scanDate = scanDate;
    }

    public String getDamage() {
        return damage;
    }

    public void setDamage(String damage) {
        this.damage = damage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPublishForImport() {
        return publishForImport;
    }

    public void setPublishForImport(String publishForImport) {
        this.publishForImport = publishForImport;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
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

    public PdaEntryHdr getEntryHdr() {
        return entryHdr;
    }

    public void setEntryHdr(PdaEntryHdr entryHdr) {
        this.entryHdr = entryHdr;
    }
}

