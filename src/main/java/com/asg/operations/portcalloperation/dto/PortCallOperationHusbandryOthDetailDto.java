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
public class PortCallOperationHusbandryOthDetailDto {
    private Long transactionPoid;
    private Long detRowId;

    @Size(max = 300)
    private String arrangement;

    @Size(max = 300)
    private String descriptionText;

    @Size(max = 1)
    private String meetGreet;

    private BigDecimal noOfDays;
    private BigDecimal qty;
    private Long unitPoid;
    private BigDecimal unitPrice;

    @Size(max = 50)
    private String currencyCode;

    private BigDecimal totalPrice;
    private BigDecimal adjustedPrice;

    @Size(max = 4000)
    private String arrngmntAttachments;

    @Size(max = 300)
    private String requestedBy;

    @Size(max = 300)
    private String paymentMode;

    private ActionType actionType;
}
