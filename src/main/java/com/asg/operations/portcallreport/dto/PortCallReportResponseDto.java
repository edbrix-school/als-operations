package com.asg.operations.portcallreport.dto;

import com.asg.operations.commonlov.dto.LovItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallReportResponseDto {
    private Long portCallReportPoid;
    private String portCallReportId;
    private String portCallReportName;
    private List<String> portCallApplVesselType;
    private List<LovItem> portCallApplVesselTypeDet;
    private String active;
    private Long seqno;
    private String remarks;
    private List<PortCallReportDetailResponseDto> details;
}
