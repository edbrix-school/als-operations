package com.asg.operations.portcalloperation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationDocsCopyDetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private String documentFrom;
    private String documentList;
    private Long emailPoid;
    private LocalDate emailSentOn;
    private String documentSelect;
    private String documentAttachments;
}
