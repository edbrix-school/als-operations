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

    @Size(max = 300, message = "Arrangement should not exceed 300 characters")
    private String arrangement;

    @Size(max = 300, message = "Description Text should not exceed 300 characters")
    private String descriptionText;

    @Size(max = 1, message = "Meet & Greet should not exceed 1 character")
    private String meetGreet;

    private BigDecimal noOfDays;
    private BigDecimal qty;
    private Long unitPoid;
    private BigDecimal unitPrice;

    @Size(max = 50, message = "Currency Code should not exceed 50 characters")
    private String currencyCode;

    private BigDecimal totalPrice;
    private BigDecimal adjustedPrice;

    @Size(max = 4000, message = "Arrangement Attachments should not exceed 4000 characters")
    private String arrngmntAttachments;

    @Size(max = 300, message = "Requested By should not exceed 300 characters")
    private String requestedBy;

    @Size(max = 300, message = "Payment Mode should not exceed 300 characters")
    private String paymentMode;

    private ActionType actionType;
}
