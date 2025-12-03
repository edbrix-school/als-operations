package com.asg.operations.pdaporttariffmaster.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class PdaPortTariffMasterResponse {
    private Long transactionPoid;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    private String docRef;

    private List<String> ports; // Array of port POIDs
    private List<String> portNames; // Array of port names (resolved)

    private List<String> vesselTypes; // Array of vessel type POIDs
    private List<String> vesselTypeNames; // Array of vessel type names (resolved)

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodTo;

    private String remarks;

    private String deleted;

    private List<PdaPortTariffChargeDetailResponse> chargeDetails;

    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;

    private String lastModifiedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastModifiedDate;

}
