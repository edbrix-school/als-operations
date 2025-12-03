package com.alsharif.operations.pdaentryform.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class PdaEntryVehicleDetailRequest {

    private Long detRowId; // null for new, existing value for update

    @Size(max = 100)
    private String vesselName;

    @Size(max = 50)
    private String voyageRef;

    @Size(max = 5)
    private String inOutMode;

    @Size(max = 100)
    private String vehicleModel;

    @Size(max = 100)
    private String vinNumber;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scanDate;

    @Size(max = 5)
    private String damage;

    @Size(max = 50)
    private String status;

    @Size(max = 1)
    private String publishForImport;

    @Size(max = 500)
    private String remarks;

    // Getters and Setters

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
}
