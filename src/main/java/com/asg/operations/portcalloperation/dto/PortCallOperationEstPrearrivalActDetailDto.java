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
public class PortCallOperationEstPrearrivalActDetailDto {
    private Long preActivityDtlPoid;

    @Valid
    private List<PortCallReportActivityDto> activities;

    private Long emailPoid;

    @Size(max = 1000, message = "Remarks should not exceed 1000 characters")
    private String remarks;

    @NotNull(message = "Send Email is required")
    private Boolean sendEmail;

    private ActionType actionType;
}
