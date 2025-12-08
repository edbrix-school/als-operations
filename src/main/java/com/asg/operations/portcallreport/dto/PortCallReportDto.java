package com.asg.operations.portcallreport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallReportDto {
    private Long portCallReportPoid;
    private String portCallReportId;
    
    @NotBlank(message = "Report name is required")
    private String portCallReportName;
    
    @NotEmpty(message = "Vessel type is required")
    private List<String> portCallApplVesselType;
    
    private String active;
    private Long seqno;
    private String remarks;
    private List<PortCallReportDetailDto> details;
}
