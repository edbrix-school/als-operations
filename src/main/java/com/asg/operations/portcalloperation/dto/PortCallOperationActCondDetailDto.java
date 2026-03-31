package com.asg.operations.portcalloperation.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class PortCallOperationActCondDetailDto {
    private Long transactionPoid;
    private Long detRowId;

    @NotBlank(message = "Condition Type is required")
    @Size(max = 100, message = "Condition Type should not exceed 100 characters")
    private String conditionType;

    @NotNull(message = "Draft Forward is required")
    private BigDecimal draftForward;
    private BigDecimal draftMid;
    @NotNull(message = "Draft Aft is required")
    private BigDecimal draftAft;
    private BigDecimal fuelOil;
    private BigDecimal dieselOil;
    private BigDecimal freshWater;
    private BigDecimal tugsService;

    private ActionType actionType;
}
