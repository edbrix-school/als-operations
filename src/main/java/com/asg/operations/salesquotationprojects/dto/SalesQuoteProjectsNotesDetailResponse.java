package com.asg.operations.salesquotationprojects.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SalesQuoteProjectsNotesDetailResponse {
    private Long transactionPoid;
    private Long detRowId;
    private String notes;
    private String createdBy;
    private LocalDate createdDate;
    private String lastModifiedBy;
    private LocalDate lastModifiedDate;
}