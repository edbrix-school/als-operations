package com.asg.operations.portcalloperation.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import com.fasterxml.jackson.annotation.JsonFormat;
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
public class PortCallOperationActRmksDetailDto {
    private Long transactionPoid;
    private Long detRowId;

    @Size(max = 100, message = "Remarks Type should not exceed 100 characters")
    private String remarksType;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime remarksFrom;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime remarksTo;

    @Size(max = 100, message = "Cargo Details should not exceed 100 characters")
    private String cargoDetails;

    @Size(max = 500, message = "Reason should not exceed 500 characters")
    private String reason;

    private Long pcReportPoid;

    private ActionType actionType;
}
