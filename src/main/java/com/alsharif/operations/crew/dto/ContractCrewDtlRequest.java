package com.alsharif.operations.crew.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request DTO for Contract Crew Detail (Visa Details)
 */
@Data
public class ContractCrewDtlRequest {

    private Long detRowId; // Required for UPDATE operations

    @NotBlank(message = "Document type is mandatory")
    @Size(max = 50, message = "Document type must not exceed 50 characters")
    private String documentType;

    @NotBlank(message = "Document number is mandatory")
    @Size(max = 100, message = "Document number must not exceed 100 characters")
    private String documentNumber;

    @NotNull(message = "Document applied date is mandatory")
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

    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    private String remarks;

    private String operation; // INSERT, UPDATE, DELETE (optional, can be inferred from detRowId presence)
}

