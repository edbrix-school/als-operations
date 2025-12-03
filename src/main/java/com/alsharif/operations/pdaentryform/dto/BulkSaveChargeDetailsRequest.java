package com.alsharif.operations.pdaentryform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;


public class BulkSaveChargeDetailsRequest {

    @NotNull(message = "Charge details list is mandatory")
    @Valid
    private List<PdaEntryChargeDetailRequest> chargeDetails;

    private List<Long> deleteDetRowIds; // IDs to delete

    // Getters and Setters

    public List<PdaEntryChargeDetailRequest> getChargeDetails() {
        return chargeDetails;
    }

    public void setChargeDetails(List<PdaEntryChargeDetailRequest> chargeDetails) {
        this.chargeDetails = chargeDetails;
    }

    public List<Long> getDeleteDetRowIds() {
        return deleteDetRowIds;
    }

    public void setDeleteDetRowIds(List<Long> deleteDetRowIds) {
        this.deleteDetRowIds = deleteDetRowIds;
    }
}