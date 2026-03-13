package com.asg.operations.portcalloperation.dto;

import com.asg.operations.portcallreport.enums.ActionType;
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
public class PortCallOperationActBunkerDetailDto {
    private Long transactionPoid;
    private Long detRowId;

    @Size(max = 100, message = "Bunker Grade should not exceed 100 characters")
    private String grade;

    private BigDecimal nominatedQtyMt;
    private BigDecimal suppliedQtyMt;
    private BigDecimal shipQtyMt;

    private ActionType actionType;
}
