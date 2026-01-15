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
public class PortCallOperationActTimingsActvtyDetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long actualsTimingDtlPoid;
    private Long activityPoid;
    private String details;
    private LocalDateTime estimatedDatetime;
}
