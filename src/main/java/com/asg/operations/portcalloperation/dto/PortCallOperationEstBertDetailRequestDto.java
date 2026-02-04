package com.asg.operations.portcalloperation.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortCallOperationEstBertDetailRequestDto {
    private Long transactionPoid;
    private Long detRowId;

    @NotNull(message = "ETA is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eta;

    @NotNull(message = "ETB is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime etb;

    @Size(max = 4000, message = "Berthing Attachments should not exceed 4000 characters")
    private String berthingAttachments;

    @Size(max = 1000, message = "Remarks should not exceed 1000 characters")
    private String remarks;

    private Long emailPoid;

    @NotNull(message = "Send Email is required")
    private Boolean sendEmail;

    private ActionType actionType;
}
