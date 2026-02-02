package com.asg.operations.portcalloperation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
