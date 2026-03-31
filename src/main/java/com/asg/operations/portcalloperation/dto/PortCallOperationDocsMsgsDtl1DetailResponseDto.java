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
public class PortCallOperationDocsMsgsDtl1DetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long emailPoid;
    private Long sendByPoid;
    private String emailSubject;
    private String emailDocuments;
    private LocalDate emailSendOn;
    private String emailContent;
    private String emailRemarks;
}
