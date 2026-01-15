package com.asg.operations.portcalloperation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
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
    private LocalDate estBlDate;
}
