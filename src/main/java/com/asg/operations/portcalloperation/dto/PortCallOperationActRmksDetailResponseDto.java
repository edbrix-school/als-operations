package com.asg.operations.portcalloperation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationActRmksDetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private String remarksType;
    private LocalDateTime remarksFrom;
    private LocalDate remarksTo;
    private String cargoDetails;
    private String reason;
    private Long pcReportPoid;
}
