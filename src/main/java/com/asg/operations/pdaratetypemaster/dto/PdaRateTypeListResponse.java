package com.asg.operations.pdaratetypemaster.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Setter
@Getter
public class PdaRateTypeListResponse {

    @JsonProperty("RATE_TYPE_ID")
    private Long rateTypeId;

    @JsonProperty("GROUP_POID")
    private Long groupPoid;

    @JsonProperty("RATE_TYPE_CODE")
    private String rateTypeCode;

    @JsonProperty("RATE_TYPE_NAME")
    private String rateTypeName;

    @JsonProperty("RATE_TYPE_NAME2")
    private String rateTypeName2;

    @JsonProperty("RATE_TYPE_FORMULA")
    private String rateTypeFormula;

    @JsonProperty("DEF_QTY")
    private String defQty;

    @JsonProperty("DEF_DAYS")
    private BigDecimal defDays;

    @JsonProperty("SEQ_NO")
    private BigInteger seqNo;

    @JsonProperty("ACTIVE")
    private String active;

    @JsonProperty("CREATED_DATE")
    private LocalDateTime createdDate;

    @JsonProperty("CREATED_BY")
    private String createdBy;

    @JsonProperty("MODIFIED_DATE")
    private LocalDateTime modifiedDate;

    @JsonProperty("MODIFIED_BY")
    private String modifiedBy;
}