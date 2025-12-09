package com.asg.operations.pdaporttariffmaster.dto;

import com.asg.operations.commonlov.dto.LovItem;
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

    private Long chargePoid;
    private LovItem chargeDet;
//    private String chargeCode;
//    private String chargeName;

    private Long rateTypePoid;
    private LovItem rateTypeDet;
//    private String rateTypeCode;
//    private String rateTypeName;

    private String tariffSlab;
    private LovItem tariffSlabDet;

    private BigDecimal fixRate;

    private String harborCallType;
    private LovItem harborCallTypeDet;

    private String isEnabled;
    private LovItem isEnabledDet;

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
