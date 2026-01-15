package com.asg.operations.portcalloperation.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationEstBertDetailDto {
    private Long transactionPoid;
    private Long detRowId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eta;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime etb;

    @Size(max = 4000)
    private String berthingAttachments;

    private Long emailPoid;

    private ActionType actionType;
}
