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
public class FdaDocumentViewResponse {
    private BigDecimal fdaPoid;
    private String fdaRef;
    private String fdaUrl;
    private String status;
}