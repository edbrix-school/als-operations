package com.asg.operations.portcalloperation.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationActCargoFigDetailDto {
    private Long transactionPoid;
    private Long detRowId;

    @Size(max = 300)
    private String cargo;

    @Size(max = 500)
    private String callType;

    private BigDecimal qty;
    private Long unitPoid;

    @Size(max = 500)
    private String vesselReq;

    @Size(max = 500)
    private String terminalNom;

    private BigDecimal shipFigureMt;
    private BigDecimal shoreFigureMt;
    private BigDecimal shipFigureBbls;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime blDate;

    private BigDecimal hoseNo;
    private BigDecimal hoseSize;

    private ActionType actionType;
}
