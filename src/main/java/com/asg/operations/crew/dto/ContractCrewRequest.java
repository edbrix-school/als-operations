package com.asg.operations.crew.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for creating/updating Contract Crew Master
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractCrewRequest {

    @NotBlank(message = "Crew name is mandatory")
    @Size(max = 250, message = "Crew name must not exceed 250 characters")
    private String crewName;

    @NotNull(message = "Nationality is mandatory")
    private Long crewNationalityPoid;

    @Size(max = 50, message = "CDC number must not exceed 50 characters")
    private String crewCdcNumber;

    @NotBlank(message = "Company is mandatory")
    @Size(max = 150, message = "Company must not exceed 150 characters")
    private String crewCompany;

    @NotBlank(message = "Designation is mandatory")
    @Size(max = 150, message = "Designation must not exceed 150 characters")
    private String crewDesignation;

    @NotBlank(message = "Passport number is mandatory")
    @Size(max = 50, message = "Passport number must not exceed 50 characters")
    private String crewPassportNumber;

    @NotNull(message = "Passport issue date is mandatory")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate crewPassportIssueDate;

    @NotNull(message = "Passport expiry date is mandatory")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate crewPassportExpiryDate;

    @Size(max = 80, message = "Passport issue place must not exceed 80 characters")
    private String crewPassportIssuePlace;

    @Size(max = 450, message = "Remarks must not exceed 450 characters")
    private String remarks;

    @Size(max = 1)
    private String active;

    @Valid
    private List<ContractCrewDtlRequest> details;
}

