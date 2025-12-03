package com.asg.operations.pdaporttariffmaster.dto;

import com.asg.operations.pdaporttariffmaster.annotation.TariffSlabValidation;
import com.asg.operations.portcallreport.enums.ActionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@TariffSlabValidation
public class PdaPortTariffChargeDetailRequest {

    private Long detRowId; // Optional, used for updates

    private BigDecimal chargePoid;

    private BigDecimal rateTypePoid;

    @Size(max = 20, message = "Tariff slab cannot exceed 20 characters")
    @Pattern(
            regexp = "NONE|SLAB|GRT_SLAB|NRT_SLAB|DWT_SLAB|RUNNING",
            message = "Invalid tariff slab. Allowed values: NONE, SLAB, GRT_SLAB, NRT_SLAB, DWT_SLAB, RUNNING"
    )
    private String tariffSlab;

    @DecimalMin(value = "0", message = "Fix rate cannot be negative")
    private BigDecimal fixRate;

    @Size(max = 50, message = "Harbor call type cannot exceed 50 characters")
    @Pattern(
            regexp = "N/A|OTHER",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Invalid harbor call type. Allowed values: N/A, OTHER"
    )
    private String harborCallType;

    @Size(max = 1, message = "Is enabled must be 'Y' or 'N'")
    private String isEnabled = "Y";

    @Size(max = 300, message = "Remarks cannot exceed 300 characters")
    private String remarks;

    private Integer seqNo;

    @Valid
    private List<PdaPortTariffSlabDetailRequest> slabDetails;

    private ActionType actionType;
}
