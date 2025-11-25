package com.alsharif.operations.crew.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for bulk save operation of crew details
 */
@Data
public class BulkSaveDetailsRequest {

    @Valid
    @NotEmpty(message = "Details list cannot be empty")
    private List<ContractCrewDtlRequest> details;

    private List<Long> deletedRowIds;

}

