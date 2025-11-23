package com.alsharif.operations.pdaporttariffmaster.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PdaPortTariffChargeDetailResponse {

    private Long detRowId;

    private BigDecimal chargePoid;
//    private String chargeCode;
//    private String chargeName;

    private BigDecimal rateTypePoid;
//    private String rateTypeCode;
//    private String rateTypeName;

    private String tariffSlab;

    private BigDecimal fixRate;

    private String harborCallType;

    private String isEnabled;

    private String remarks;

    private Integer seqNo;

    private List<PdaPortTariffSlabDetailResponse> slabDetails;

    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;

    private String lastModifiedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastModifiedDate;

}
