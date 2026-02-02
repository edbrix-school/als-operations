package com.asg.operations.portcalloperation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationEstBertDetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private LocalDateTime eta;
    private LocalDateTime etb;
    private LocalDateTime updatedOn;
    private String updatedBy;
    private LocalDateTime emailSentOn;
    private String remarks;
    private String berthingAttachments;
    private Long emailPoid;
}
