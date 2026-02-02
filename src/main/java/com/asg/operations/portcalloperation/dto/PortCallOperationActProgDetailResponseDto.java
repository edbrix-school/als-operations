package com.asg.operations.portcalloperation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationActProgDetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long emailPoid;
    private String cargo;
    private LocalDateTime progressDateTime;
    private BigDecimal progressQty;
    private String progressStatus;
    private BigDecimal balanceQty;
    private Long unitPoid;
    private BigDecimal ratePerHr;
    private LocalDateTime etc;
    private LocalDateTime emailSendOn;
    private LocalDate estBlDate;
}
