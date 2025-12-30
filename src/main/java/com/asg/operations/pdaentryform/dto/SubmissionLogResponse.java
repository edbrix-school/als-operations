package com.asg.operations.pdaentryform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionLogResponse {
    private String reportUrl;
    private String docRef;
    private String fdaRef;
    private Long transactionPoid;
    private BigDecimal fdaPoid;
}