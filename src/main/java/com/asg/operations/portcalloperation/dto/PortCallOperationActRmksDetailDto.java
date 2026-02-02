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
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationActRmksDetailDto {
    private Long transactionPoid;
    private Long detRowId;

    @Size(max = 100)
    private String remarksType;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime remarksFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate remarksTo;

    @Size(max = 100)
    private String cargoDetails;

    @Size(max = 500)
    private String reason;

    private Long pcReportPoid;

    private ActionType actionType;
}
