package com.asg.operations.portcalloperation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationEstPrearrivalActDetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long preActivityDtlPoid;
    private Long activityPoid;
    private String otherDescription;
    private LocalDateTime estimatedDatetime;
}
