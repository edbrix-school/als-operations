package com.alsharif.operations.crew.dto;

import java.util.List;

/**
 * Response DTO for crew details list
 */
public class CrewDetailsResponse {

    private Long crewPoid;
    private List<ContractCrewDtlResponse> details;

    // Getters and Setters
    public Long getCrewPoid() {
        return crewPoid;
    }

    public void setCrewPoid(Long crewPoid) {
        this.crewPoid = crewPoid;
    }

    public List<ContractCrewDtlResponse> getDetails() {
        return details;
    }

    public void setDetails(List<ContractCrewDtlResponse> details) {
        this.details = details;
    }
}

