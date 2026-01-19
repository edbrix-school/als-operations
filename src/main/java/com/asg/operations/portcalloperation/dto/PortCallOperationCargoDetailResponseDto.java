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
public class PortCallOperationCargoDetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private String productName;
    private String portCargoName;
    private BigDecimal qtyMt;
    private BigDecimal qtyCbm;
    private BigDecimal noOfQty;
    private String callType;
    private Long portOfCallPoid;
    private String berth;
    private String shipper;
    private String receiver;
}
