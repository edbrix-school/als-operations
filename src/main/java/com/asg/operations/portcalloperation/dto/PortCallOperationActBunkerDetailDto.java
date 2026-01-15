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
public class PortCallOperationActBunkerDetailDto {
    private Long transactionPoid;
    private Long detRowId;

    @Size(max = 100)
    private String grade;

    private BigDecimal nominatedQtyMt;
    private BigDecimal suppliedQtyMt;
    private BigDecimal shipQtyMt;

    private ActionType actionType;
}
