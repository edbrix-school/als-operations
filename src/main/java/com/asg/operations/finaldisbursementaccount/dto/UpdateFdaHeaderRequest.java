package com.asg.operations.finaldisbursementaccount.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class UpdateFdaHeaderRequest {

    @NotNull(message = "Principal is required")
    private Long principalPoid;

    @NotNull(message = "Salesman is required")
    private Long salesmanPoid;

    @NotNull(message = "Port is required")
    private Long portPoid;

    @Size(max = 2000, message = "Remarks cannot exceed 2000 characters")
    private String remarks;

    private String cargoDetails;

    private String operationType;

    private String harbourCallType;

    private String unit;

    private BigDecimal numberOfDays;

    private String portDescription;

    private String fdaSubType;

    private String subCategory;

    @NotNull(message = "GRT is required")
    private BigDecimal grt;

    private BigDecimal nrt;
    private BigDecimal dwt;
    private List<FdaChargeDto> charges;
}
