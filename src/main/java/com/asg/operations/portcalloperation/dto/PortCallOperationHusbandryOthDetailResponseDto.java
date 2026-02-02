package com.asg.operations.portcalloperation.dto;

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
public class PortCallOperationHusbandryOthDetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private String arrangement;
    private String descriptionText;
    private String meetGreet;
    private BigDecimal noOfDays;
    private BigDecimal qty;
    private Long unitPoid;
    private BigDecimal unitPrice;
    private String currencyCode;
    private BigDecimal totalPrice;
    private BigDecimal adjustedPrice;
    private String arrngmntAttachments;
    private String requestedBy;
    private String paymentMode;
}
