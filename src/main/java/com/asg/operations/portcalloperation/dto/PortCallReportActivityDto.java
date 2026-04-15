package com.asg.operations.portcalloperation.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PortCallReportActivityDto {
    private Long activityPoid;

    // Nullable when activityPoid is not provided; required when activityPoid is null.
    @Size(max = 300, message = "activityName should not exceed 300 characters")
    private String activityName;

    @Size(max = 300,message = "Details should not exceed 300 character")
    private String otherDescription;

    private LocalDateTime estimatedDatetime;
}
