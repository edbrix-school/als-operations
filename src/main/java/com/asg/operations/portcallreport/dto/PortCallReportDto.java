package com.asg.operations.portcallreport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
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
    @Size(max = 50, message = "Port Call Report ID cannot exceed 50 characters")
    private String portCallReportId;

    @NotBlank(message = "Port Call Report Name is required")
    @Size(max = 300, message = "Port Call Report Name cannot exceed 300 characters")
    private String portCallReportName;

    @NotEmpty(message = "Vessel type is required")
    private List<String> portCallApplVesselType;

    @Size(max = 1, message = "Active cannot exceed 1 character")
    private String active;
    private Long seqno;
    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters")
    private String remarks;
    private List<PortCallReportDetailDto> details;
}
