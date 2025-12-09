package com.asg.operations.pdaentryform.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for PDA Entry Charge Detail
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdaEntryChargeDetailResponse {

    private Long transactionPoid;
    private Long detRowId;
    private BigDecimal chargePoid;
    private BigDecimal rateTypePoid;
    private BigDecimal principalPoid;
    private String currencyCode;
    private BigDecimal currencyRate;
    private BigDecimal qty;
    private BigDecimal days;
    private BigDecimal pdaRate;
    private BigDecimal taxPoid;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private BigDecimal amount;
    private BigDecimal fdaAmount;
    private String fdaDocRef;
    private BigDecimal fdaPoid;
    private String fdaCreationType;
    private String dataSource;
    private String detailFrom;
    private String manual;
    private Integer seqno;
    private String remarks;
    private String oldChargeCode;
    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;

    private String lastModifiedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastModifiedDate;
}

