package com.asg.operations.portcalloperation.dto;

import com.asg.operations.portcallreport.enums.ActionType;
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
    private LocalDateTime eta;

    @NotNull(message = "ETB is required")
    private LocalDateTime etb;

    @Size(max = 4000, message = "Berthing Attachments should not exceed 4000 characters")
    private String berthingAttachments;

    private Long emailPoid;

    private ActionType actionType;
}
