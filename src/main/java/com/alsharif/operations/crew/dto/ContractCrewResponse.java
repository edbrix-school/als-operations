package com.alsharif.operations.crew.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for Contract Crew Master
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractCrewResponse {

    private Long crewPoid;
    private String crewName;
    private Long crewNationalityPoid;
    private String crewNationalityCode;
    private String crewNationalityName;
    private String crewCdcNumber;
    private String crewCompany;
    private String crewDesignation;
    private String crewPassportNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate crewPassportIssueDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate crewPassportExpiryDate;

    private String crewPassportIssuePlace;
    private String remarks;
    private String active;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;

    private String createdBy;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;

}

