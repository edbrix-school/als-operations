package com.asg.operations.portcalloperation.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationEstPrearrivalDetailDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long preActivityDtlPoid;

    @NotNull(message = "ETA is required")
    private LocalDateTime eta;

    @NotNull(message = "ETB is required")
    private LocalDateTime etb;

    @Size(max = 4000, message = "Pre-arrival attachments should not exceed 4000 characters")
    private String preArrivalAttachments;

    private Long emailPoid;

    private ActionType actionType;
}
