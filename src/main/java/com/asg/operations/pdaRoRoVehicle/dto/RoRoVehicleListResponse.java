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

    @JsonProperty("TRANSACTION_DATE")
    private LocalDate transactionDate;

    @JsonProperty("DOC_REF")
    private String docRef;

    @JsonProperty("LINE_NAME")
    private String lineName;

    @JsonProperty("VOYAGE_NO")
    private String VoyageNo;

    @JsonProperty("PERIOD_FROM")
    private LocalDate periodFrom;

    @JsonProperty("PERIOD_TO")
    private LocalDate periodTo;

    @JsonProperty("VESSEL_NAME")
    private String vesselName;

    @JsonProperty("DELETED")
    private String deleted;

    @JsonProperty("CREATED_BY")
    private String createdBy;

    @JsonProperty("CREATED_DATE")
    private LocalDateTime createdDate;

    @JsonProperty("LAST_MODIFIED_BY")
    private String lastModifiedBy;

    @JsonProperty("LAST_MODIFIED_DATE")
    private LocalDateTime lastModifiedDate;
}
