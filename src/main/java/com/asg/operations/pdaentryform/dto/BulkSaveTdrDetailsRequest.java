package com.asg.operations.pdaentryform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request DTO for bulk save of TDR details
 */
public class BulkSaveTdrDetailsRequest {

    @NotNull(message = "TDR details list is mandatory")
    @Valid
    private List<PdaEntryTdrDetailRequest> tdrDetails;

    private List<Long> deleteDetRowIds; // IDs to delete

    // Getters and Setters

    public List<PdaEntryTdrDetailRequest> getTdrDetails() {
        return tdrDetails;
    }

    public void setTdrDetails(List<PdaEntryTdrDetailRequest> tdrDetails) {
        this.tdrDetails = tdrDetails;
    }

    public List<Long> getDeleteDetRowIds() {
        return deleteDetRowIds;
    }

    public void setDeleteDetRowIds(List<Long> deleteDetRowIds) {
        this.deleteDetRowIds = deleteDetRowIds;
    }
}

