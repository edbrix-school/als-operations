package com.asg.operations.pdaentryform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for bulk save of acknowledgment details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkSaveAcknowledgmentDetailsRequest {

    @NotNull(message = "Acknowledgment details list is mandatory")
    @Valid
    private List<PdaEntryAcknowledgmentDetailRequest> acknowledgmentDetails;

    private List<Long> deleteDetRowIds; // IDs to delete
}

