package com.asg.operations.portcalloperation.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
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
public class PortCallOperationDocsMsgsDtl1DetailDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long emailPoid;
    private Long sendByPoid;

    @Size(max = 1000)
    private String emailSubject;

    @Size(max = 4000)
    private String emailDocuments;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate emailSendOn;

    @Size(max = 4000)
    private String emailContent;

    @Size(max = 1000)
    private String emailRemarks;

    private ActionType actionType;
}
