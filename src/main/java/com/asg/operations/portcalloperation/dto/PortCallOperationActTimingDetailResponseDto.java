package com.asg.operations.portcalloperation.dto;

import com.asg.common.lib.dto.LovGetListDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationActTimingDetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long portReportPoid;
    private LovGetListDto portReportDet;
    private Long actualsTimingDtlPoid;
    private Long emailPoid;
}
