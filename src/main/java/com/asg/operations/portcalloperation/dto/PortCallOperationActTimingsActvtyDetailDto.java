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
public class PortCallOperationActTimingsActvtyDetailDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long actualsTimingDtlPoid;
    private Long activityPoid;

    @Size(max = 300)
    private String details;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime estimatedDatetime;

    @NotNull(message = "Send Email is required")
    private Boolean sendEmail;

    private ActionType actionType;
}
