package com.alsharif.operations.pdaporttariffmaster.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PdaPortTariffMasterRequest {

    @NotNull(message = "At least one port must be selected")
    @NotEmpty(message = "Ports list cannot be empty")
    private List<String> ports; // List of port POIDs as strings

    @NotNull(message = "At least one vessel type must be selected")
    @NotEmpty(message = "Vessel types list cannot be empty")
    private List<String> vesselTypes; // List of vessel type POIDs as strings

    @NotNull(message = "Period from date is mandatory")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodFrom;

    @NotNull(message = "Period to date is mandatory")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodTo;

    private String remarks;

    private String docRef;

    @Valid
    private List<PdaPortTariffChargeDetailRequest> chargeDetails;
}
