package com.asg.operations.salesquotationprojects.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SalesQuoteProjectsChargeDetailRequest {
    private Long detRowId;
    private Long chargePoid;
    
    @Size(max = 500, message = "Printable charge description cannot exceed 500 characters")
    private String printableChargeDesc;
    
    private BigDecimal quantity;
    private Long unitPoid;
    
    @Size(max = 20, message = "Buy currency code cannot exceed 20 characters")
    private String buyCurrencyCode;
    
    private BigDecimal buyCurrencyRate;
    private BigDecimal buyUnitRate;
    private BigDecimal buyTotalLc;
    private BigDecimal sellUnitRateFc;
    private BigDecimal sellTotalFc;
    private Long taxPoid;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmountFc;
    private BigDecimal sellGrandTotalFc;
    private BigDecimal taxAmountLc;
    private BigDecimal sellGrandTotalLc;
    
    @Size(max = 100, message = "Remarks cannot exceed 100 characters")
    private String remarks;
    
    private ActionType actionType;
}