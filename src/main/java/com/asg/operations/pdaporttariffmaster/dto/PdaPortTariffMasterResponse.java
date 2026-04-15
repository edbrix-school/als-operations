package com.asg.operations.pdaporttariffmaster.dto;

import com.asg.operations.commonlov.dto.LovItem;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class PdaPortTariffMasterResponse {
    private Long transactionPoid;

    private LocalDate transactionDate;

    private String docRef;

    private String port; // Port POID
    private LovItem portDet;
    private String portName; // Port name (resolved)

    private List<String> vesselTypes; // Array of vessel type POIDs
    private List<LovItem> vesselTypesDet;
    private List<String> vesselTypeNames; // Array of vessel type names (resolved)

    private Long groupPoid;
    private LovItem groupDet;
    private Long companyPoid;
    private LovItem companyDet;

    private LocalDate periodFrom;

    private LocalDate periodTo;

    private String remarks;

    private String deleted;

    private List<PdaPortTariffChargeDetailResponse> chargeDetails;

    private String createdBy;

    private LocalDateTime createdDate;

    private String lastModifiedBy;

    private LocalDateTime lastModifiedDate;
}
