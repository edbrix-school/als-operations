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
public class PortCallOperationEstBertDetailDto {
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

    private Long emailPoid;

    private ActionType actionType;
}
