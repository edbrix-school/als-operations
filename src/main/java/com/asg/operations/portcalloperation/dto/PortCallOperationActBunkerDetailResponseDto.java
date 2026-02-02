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
public class PortCallOperationActBunkerDetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private String grade;
    private BigDecimal nominatedQtyMt;
    private BigDecimal suppliedQtyMt;
    private BigDecimal shipQtyMt;
}
