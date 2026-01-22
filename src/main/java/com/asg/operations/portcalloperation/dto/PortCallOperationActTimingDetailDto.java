package com.asg.operations.portcalloperation.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationActTimingDetailDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long portReportPoid;
    private Long actualsTimingDtlPoid;
    private Long emailPoid;

    private ActionType actionType;
}
