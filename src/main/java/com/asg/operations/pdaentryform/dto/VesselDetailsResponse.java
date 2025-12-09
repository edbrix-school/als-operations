package com.asg.operations.pdaentryform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for vessel details (auto-populated from LOV change)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VesselDetailsResponse {

    private BigDecimal vesselTypePoid;
    private String imoNumber;
    private BigDecimal grt;
    private BigDecimal nrt;
    private BigDecimal dwt;

}

