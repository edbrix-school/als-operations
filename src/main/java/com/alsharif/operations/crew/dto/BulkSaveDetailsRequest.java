package com.alsharif.operations.crew.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Request DTO for bulk save operation of crew details
 */
public class BulkSaveDetailsRequest {

    @Valid
    @NotEmpty(message = "Details list cannot be empty")
    private List<ContractCrewDtlRequest> details;

    private List<Long> deletedRowIds;

    // Getters and Setters
    public List<ContractCrewDtlRequest> getDetails() {
        return details;
    }

    public void setDetails(List<ContractCrewDtlRequest> details) {
        this.details = details;
    }

    public List<Long> getDeletedRowIds() {
        return deletedRowIds;
    }

    public void setDeletedRowIds(List<Long> deletedRowIds) {
        this.deletedRowIds = deletedRowIds;
    }
}

