package com.alsharif.operations.portcallreport.dto;

import com.alsharif.operations.commonlov.dto.LovItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallReportDetailResponseDto {
    private Long portCallReportPoid;
    private Long detRowId;
    private Long portActivityTypePoid;
    private String portActivityTypeName;
    private String activityMandatory;
    private LovItem portActivityDet;
}
