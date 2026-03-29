package com.asg.operations.portcalloperation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationActRmksDetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private String remarksType;
    private LocalDateTime remarksFrom;
    private LocalDateTime remarksTo;
    private String cargoDetails;
    private String reason;
    private Long pcReportPoid;
}
