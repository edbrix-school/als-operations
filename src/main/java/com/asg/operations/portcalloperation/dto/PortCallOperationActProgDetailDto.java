package com.asg.operations.portcalloperation.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationActProgDetailDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long emailPoid;

    @Size(max = 300)
    private String cargo;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime progressDateTime;

    private BigDecimal progressQty;

    @Size(max = 100)
    private String progressStatus;

    private BigDecimal balanceQty;
    private Long unitPoid;
    private BigDecimal ratePerHr;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime etc;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate estBlDate;

    private ActionType actionType;
}
