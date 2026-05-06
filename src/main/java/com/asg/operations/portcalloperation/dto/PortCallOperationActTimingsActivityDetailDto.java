package com.asg.operations.portcalloperation.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortCallOperationActTimingsActivityDetailDto {
    private Long actualsTimingDtlPoid;

    @NotNull(message = "Port Call Report Poid is required")
    private Long portCallReportPoid;

    @Valid
    private List<PortCallReportActivityDto> activities;

    @Size(max = 1000, message = "Remarks should not exceed 1000 characters")
    private String remarks;

    private Long emailPoid;

    @NotNull(message = "Send Email is required")
    private Boolean sendEmail;

    @Valid
    private List<PortCallOperationMailDetailDto> mailDetails;

    private ActionType actionType;
}
