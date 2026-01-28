package com.asg.operations.portcalloperation.dto;

import com.asg.common.lib.dto.LovGetListDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationDocsMsgsDtl1DetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long emailPoid;
    private Long sendByPoid;
    private LovGetListDto sendByDet;
    private String emailSubject;
    private String emailDocuments;
    private LocalDate emailSendOn;
    private String emailContent;
    private String emailRemarks;
}
