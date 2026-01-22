package com.asg.operations.portcalloperation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationActCondDetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private String conditionType;
    private BigDecimal draftForward;
    private BigDecimal draftMid;
    private BigDecimal draftAft;
    private BigDecimal fuelOil;
    private BigDecimal dieselOil;
    private BigDecimal freshWater;
    private BigDecimal tugsService;
}
