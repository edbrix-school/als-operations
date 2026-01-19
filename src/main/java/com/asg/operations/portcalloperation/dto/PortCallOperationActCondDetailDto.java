package com.asg.operations.portcalloperation.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationActCondDetailDto {
    private Long transactionPoid;
    private Long detRowId;

    @Size(max = 100)
    private String conditionType;

    private BigDecimal draftForward;
    private BigDecimal draftMid;
    private BigDecimal draftAft;
    private BigDecimal fuelOil;
    private BigDecimal dieselOil;
    private BigDecimal freshWater;
    private BigDecimal tugsService;

    private ActionType actionType;
}
