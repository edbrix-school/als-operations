package com.asg.operations.pdaporttariffmaster.dto;

import com.asg.operations.pdaporttariffmaster.annotation.QuantityRangeAndSlab;
import com.asg.operations.portcallreport.enums.ActionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@QuantityRangeAndSlab
public class PdaPortTariffSlabDetailRequest {

    private Long detRowId; // Optional, used for updates

    @DecimalMin(value = "0", message = "Quantity from cannot be negative")
    private BigDecimal quantityFrom;

    @DecimalMin(value = "0", message = "Quantity to cannot be negative")
    private BigDecimal quantityTo;

    @Min(value = 0, message = "Days1 cannot be negative")
    private Long days1;

    @DecimalMin(value = "0", message = "Rate1 cannot be negative")
    private BigDecimal rate1;

    @Min(value = 0, message = "Days2 cannot be negative")
    private Long days2;

    @DecimalMin(value = "0", message = "Rate2 cannot be negative")
    private BigDecimal rate2;

    @Min(value = 0, message = "Days3 cannot be negative")
    private Long days3;

    @DecimalMin(value = "0", message = "Rate3 cannot be negative")
    private BigDecimal rate3;

    @Min(value = 0, message = "Days4 cannot be negative")
    private Long days4;

    @DecimalMin(value = "0", message = "Rate4 cannot be negative")
    private BigDecimal rate4;

    @Pattern(regexp = "Y|N", message = "Call by port must be 'Y' or 'N'")
    private String callByPort;

    @Size(max = 200, message = "Remarks cannot exceed 200 characters")
    private String remarks;

    private ActionType actionType;
}
