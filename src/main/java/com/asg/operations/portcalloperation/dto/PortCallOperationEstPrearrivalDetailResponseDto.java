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
public class PortCallOperationEstPrearrivalDetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long preActivityDtlPoid;
    private LocalDateTime eta;
    private LocalDateTime etb;
    private LocalDateTime updatedOn;
    private String updatedBy;
    private LocalDateTime emailSentOn;
    private String remarks;
    private String preArrivalAttachments;
    private Long emailPoid;
}
