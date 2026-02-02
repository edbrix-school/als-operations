package com.asg.operations.portcalloperation.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationCargoDetailDto {
    private Long detRowId;

    @Size(max = 300)
    private String productName;

    @Size(max = 300)
    private String portCargoName;

    private BigDecimal qtyMt;
    private BigDecimal qtyCbm;
    private BigDecimal noOfQty;

    @Size(max = 100)
    private String callType;

    private Long portOfCallPoid;

    @Size(max = 100)
    private String berth;

    @Size(max = 300)
    private String shipper;

    @Size(max = 300)
    private String receiver;

    private ActionType actionType;
}
