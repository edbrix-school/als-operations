package com.asg.operations.crew.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractCrewListResponse {

    @JsonProperty("CREW_POID")
    private Long crewPoid;

    @JsonProperty("CREW_NAME")
    private String crewName;

    @JsonProperty("GROUP_POID")
    private Long groupPoid;

    @JsonProperty("CREW_NATIONALITY_POID")
    private Long crewNationalityPoid;

    @JsonProperty("CREW_NATIONALITY_CODE")
    private String crewNationalityCode;

    @JsonProperty("CREW_NATIONALITY_NAME")
    private String crewNationalityName;

    @JsonProperty("CREW_CDC_NUMBER")
    private String crewCdcNumber;

    @JsonProperty("CREW_COMPANY")
    private String crewCompany;

    @JsonProperty("CREW_DESIGNATION")
    private String crewDesignation;

    @JsonProperty("CREW_PASSPORT_NUMBER")
    private String crewPassportNumber;

    @JsonProperty("CREW_PASSPORT_ISSUE_DATE")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate crewPassportIssueDate;

    @JsonProperty("CREW_PASSPORT_EXPIRY_DATE")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate crewPassportExpiryDate;

    @JsonProperty("CREW_PASSPORT_ISSUE_PLACE")
    private String crewPassportIssuePlace;

    @JsonProperty("REMARKS")
    private String remarks;

    @JsonProperty("ACTIVE")
    private String active;

    @JsonProperty("CREATED_DATE")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;

    @JsonProperty("CREATED_BY")
    private String createdBy;

    @JsonProperty("LASTMODIFIED_BY")
    private String lastModifiedBy;

    @JsonProperty("LASTMODIFIED_DATE")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastModifiedDate;

    @JsonProperty("COMPANY_POID")
    private Long companyPoid;
}