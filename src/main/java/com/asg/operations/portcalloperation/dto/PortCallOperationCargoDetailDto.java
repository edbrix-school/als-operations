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

    @Size(max = 300, message = "Product Name should not exceed 300 characters")
    private String productName;

    @Size(max = 300, message = "Port Cargo Name should not exceed 300 characters")
    private String portCargoName;

    private BigDecimal qtyMt;
    private BigDecimal qtyCbm;
    private BigDecimal noOfQty;

    @Size(max = 50, message = "UOM should not exceed 50 characters")
    private String uom;

    @Size(max = 100, message = "Call Type should not exceed 100 characters")
    private String callType;

    private Long portOfCallPoid;

    @Size(max = 100, message = "Berth should not exceed 100 characters")
    private String berth;

    @Size(max = 300, message = "Shipper should not exceed 300 characters")
    private String shipper;

    @Size(max = 300, message = "Receiver should not exceed 300 characters")
    private String receiver;

    private ActionType actionType;
}
