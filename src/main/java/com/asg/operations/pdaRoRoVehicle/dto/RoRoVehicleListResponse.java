package com.asg.operations.pdaRoRoVehicle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class RoRoVehicleListResponse {
    @JsonProperty("TRANSACTION_POID")
    private Long transactionPoid;

    @JsonProperty("COMPANY_POID")
    private Long companyPoid;

    @JsonProperty("TRANSACTION_DATE")
    private LocalDate transactionDate;

    @JsonProperty("DOC_REF")
    private String docRef;

    @JsonProperty("LINE_NAME")
    private String lineName;

    @JsonProperty("VOYAGE_NO")
    private String VoyageNo;

    @JsonProperty("VESSEL_NAME")
    private String vesselName;

    @JsonProperty("DELETED")
    private String deleted;
}
