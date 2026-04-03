package com.asg.operations.portcallreport.dto;

import com.asg.common.lib.dto.HasDetRowId;
import com.asg.operations.commonlov.dto.LovItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallReportDetailResponseDto implements HasDetRowId {
    private Long portCallReportPoid;
    private Long detRowId;
    private Long portActivityTypePoid;
    private String portActivityTypeName;
    private String activityMandatory;
    private LovItem portActivityDet;
}
