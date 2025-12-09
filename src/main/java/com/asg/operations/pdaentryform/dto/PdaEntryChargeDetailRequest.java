package com.asg.operations.pdaentryform.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating/updating PDA Entry Charge Detail
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdaEntryChargeDetailRequest {

    private Long detRowId; // null for new, existing value for update

    @NotNull(message = "Charge POID is mandatory")
    private BigDecimal chargePoid;

    private BigDecimal rateTypePoid;

    private BigDecimal principalPoid;

    @Size(max = 20)
    private String currencyCode;

    private BigDecimal currencyRate;

    @NotNull(message = "Quantity is mandatory")
    private BigDecimal qty;

    @NotNull(message = "Days is mandatory")
    private BigDecimal days;

    @NotNull(message = "PDA Rate is mandatory")
    private BigDecimal pdaRate;

    private BigDecimal taxPoid;

    private BigDecimal taxPercentage;

    private BigDecimal taxAmount;

    private BigDecimal amount;

    private BigDecimal fdaAmount;

    @Size(max = 100)
    private String fdaDocRef;

    private BigDecimal fdaPoid;

    @Size(max = 100)
    private String fdaCreationType;

    @Size(max = 20)
    private String dataSource;

    @Size(max = 100)
    private String detailFrom;

    @Size(max = 1)
    private String manual;

    private Integer seqno;

    @Size(max = 500)
    private String remarks;

}

