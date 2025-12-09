package com.asg.operations.pdaentryform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for bulk save of TDR details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkSaveTdrDetailsRequest {

    @NotNull(message = "TDR details list is mandatory")
    @Valid
    private List<PdaEntryTdrDetailRequest> tdrDetails;

    private List<Long> deleteDetRowIds; // IDs to delete
}

