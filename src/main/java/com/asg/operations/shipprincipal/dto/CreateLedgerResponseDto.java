package com.asg.operations.shipprincipal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for Create Ledger response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLedgerResponseDto {
    private Boolean success;
    private String message;
    private Long glCodePoid;
    private String glAcctno;
    private String errorCode;
}
