package com.asg.operations.crew.dto;

import lombok.Data;

import java.util.List;

/**
 * Response DTO for crew details list
 */
@Data
public class CrewDetailsResponse {

    private Long crewPoid;
    private List<ContractCrewDtlResponse> details;
}

