package com.asg.operations.portcalloperation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationEstPrearrivalActDetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long preActivityDtlPoid;
    private Long activityPoid;
    private String activityMandatory;
    private String activityName;
    private String otherDescription;
    private LocalDateTime estimatedDatetime;
}
