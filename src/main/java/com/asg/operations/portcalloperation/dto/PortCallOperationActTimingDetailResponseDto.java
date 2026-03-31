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
public class PortCallOperationActTimingDetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long portReportPoid;
    private Long actualsTimingDtlPoid;
    private Long emailPoid;
    private String details;
    private LocalDateTime sentStatus;
}
