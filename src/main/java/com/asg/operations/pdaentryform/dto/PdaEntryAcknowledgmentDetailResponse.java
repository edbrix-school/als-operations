package com.asg.operations.pdaentryform.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for PDA Entry Acknowledgment Detail
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdaEntryAcknowledgmentDetailResponse {

    private Long transactionPoid;
    private Long detRowId;
    private String particulars;
    private String selected;
    private String remarks;
    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;

    private String lastModifiedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastModifiedDate;
}

