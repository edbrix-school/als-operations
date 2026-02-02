package com.asg.operations.portcalloperation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationActCargoFigDetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private String cargo;
    private String callType;
    private BigDecimal qty;
    private Long unitPoid;
    private String vesselReq;
    private String terminalNom;
    private BigDecimal shipFigureMt;
    private BigDecimal shoreFigureMt;
    private BigDecimal shipFigureBbls;
    private LocalDateTime blDate;
    private BigDecimal hoseNo;
    private BigDecimal hoseSize;
}
