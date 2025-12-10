package com.asg.operations.pdaratetypemaster.dto;

import com.asg.operations.commonlov.dto.LovItem;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Setter
@Getter
public class PdaRateTypeResponseDTO {

    private Long rateTypeId;
    private Long groupPoid;
    private LovItem groupDet;
    private String rateTypeCode;
    private String rateTypeName;
    private String rateTypeName2;
    private String rateTypeFormula;
    private String defQty;
    private BigDecimal defDays;
    private BigInteger seqNo;
    private String active;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;

    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime modifiedDate;

    private String modifiedBy;
}
