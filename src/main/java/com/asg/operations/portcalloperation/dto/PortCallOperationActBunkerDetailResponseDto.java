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
public class PortCallOperationActBunkerDetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private String grade;
    private BigDecimal nominatedQtyMt;
    private BigDecimal suppliedQtyMt;
    private BigDecimal shipQtyMt;
}
