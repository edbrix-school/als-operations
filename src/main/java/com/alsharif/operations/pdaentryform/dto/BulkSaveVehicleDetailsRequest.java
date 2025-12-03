package com.alsharif.operations.pdaentryform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request DTO for bulk save of vehicle details
 */
public class BulkSaveVehicleDetailsRequest {

    @NotNull(message = "Vehicle details list is mandatory")
    @Valid
    private List<PdaEntryVehicleDetailRequest> vehicleDetails;

    private List<Long> deleteDetRowIds; // IDs to delete

    // Getters and Setters

    public List<PdaEntryVehicleDetailRequest> getVehicleDetails() {
        return vehicleDetails;
    }

    public void setVehicleDetails(List<PdaEntryVehicleDetailRequest> vehicleDetails) {
        this.vehicleDetails = vehicleDetails;
    }

    public List<Long> getDeleteDetRowIds() {
        return deleteDetRowIds;
    }

    public void setDeleteDetRowIds(List<Long> deleteDetRowIds) {
        this.deleteDetRowIds = deleteDetRowIds;
    }
}

