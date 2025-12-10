package com.asg.operations.pdaratetypemaster.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;

@Getter
@Setter
public class PdaRateTypeRequestDTO {
    @NotBlank(message = "Rate type code is mandatory")
    @Size(max = 20, message = "Rate type code must not exceed 20 characters")
    private String rateTypeCode;

    @NotBlank(message = "Rate type name is mandatory")
    @Size(max = 100, message = "Rate type name must not exceed 100 characters")
    private String rateTypeName;

    @Size(max = 100, message = "Rate type name2 must not exceed 100 characters")
    private String rateTypeName2;

    @Size(max = 1000, message = "Rate type formula must not exceed 1000 characters")
    private String rateTypeFormula;

    @Size(max = 100, message = "Default quantity must not exceed 100 characters")
    private String defQty;

    private BigDecimal defDays;

    private BigInteger seqNo;

    @Size(max = 1)
    private String active;
}

