package com.asg.operations.crew.dto;

import com.asg.operations.commonlov.dto.LovItem;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for Contract Crew Detail (Visa Details)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractCrewDtlResponse {

    private Long crewPoid;
    private Long detRowId;
    private String documentType;
    private LovItem documentTypeDet;
    private String documentTypeCode;
    private String documentTypeName;
    private String documentNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate documentAppliedDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate documentIssueDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate documentExpiryDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate pptReceiptDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate pptReturnDate;

    private String remarks;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;

    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastModifiedDate;
    private String lastModifiedBy;
    private String active;

}

