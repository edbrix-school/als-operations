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
public class PortCallOperationActTimingsActvtyDetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long actualsTimingDtlPoid;
    private Long activityPoid;
    private String details;
    private LocalDateTime estimatedDatetime;
}
