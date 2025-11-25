package com.alsharif.operations.portcallreport.dto;

import com.alsharif.operations.commonlov.dto.LovItem;
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
    private List<LovItem> portCallApplVesselType;
    private String active;
    private Long seqno;
    private String remarks;
    private List<PortCallReportDetailResponseDto> details;
}
