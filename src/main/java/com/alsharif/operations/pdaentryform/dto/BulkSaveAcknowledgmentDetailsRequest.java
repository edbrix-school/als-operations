package com.alsharif.operations.pdaentryform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request DTO for bulk save of acknowledgment details
 */
public class BulkSaveAcknowledgmentDetailsRequest {

    @NotNull(message = "Acknowledgment details list is mandatory")
    @Valid
    private List<PdaEntryAcknowledgmentDetailRequest> acknowledgmentDetails;

    private List<Long> deleteDetRowIds; // IDs to delete

    // Getters and Setters

    public List<PdaEntryAcknowledgmentDetailRequest> getAcknowledgmentDetails() {
        return acknowledgmentDetails;
    }

    public void setAcknowledgmentDetails(List<PdaEntryAcknowledgmentDetailRequest> acknowledgmentDetails) {
        this.acknowledgmentDetails = acknowledgmentDetails;
    }

    public List<Long> getDeleteDetRowIds() {
        return deleteDetRowIds;
    }

    public void setDeleteDetRowIds(List<Long> deleteDetRowIds) {
        this.deleteDetRowIds = deleteDetRowIds;
    }
}

