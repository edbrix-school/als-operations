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
public class PortCallOperationEstBertDetailRequestDto {
    @NotNull(message = "ETA is required")
    private LocalDateTime eta;

    @NotNull(message = "ETB is required")
    private LocalDateTime etb;

    @Size(max = 1000, message = "Remarks should not exceed 1000 characters")
    private String remarks;

    private Long emailPoid;

    @NotNull(message = "Send Email is required")
    private Boolean sendEmail;

    private ActionType actionType;
}
