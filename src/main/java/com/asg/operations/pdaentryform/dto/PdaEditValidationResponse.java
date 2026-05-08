package com.asg.operations.pdaentryform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdaEditValidationResponse {
    private String status;
    private String message;
    private Boolean canEdit;
    private String fdaTransactionPoid;
}
