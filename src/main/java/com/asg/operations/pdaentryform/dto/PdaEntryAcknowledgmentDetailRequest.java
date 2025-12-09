package com.asg.operations.pdaentryform.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating/updating PDA Entry Acknowledgment Detail
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdaEntryAcknowledgmentDetailRequest {

    private Long detRowId; // null for new, existing value for update

    @Size(max = 2000)
    private String particulars;

    @Size(max = 1)
    private String selected;

    @Size(max = 4000)
    private String remarks;
}