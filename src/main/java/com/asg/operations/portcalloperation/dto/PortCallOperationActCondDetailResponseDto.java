package com.asg.operations.portcalloperation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
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
