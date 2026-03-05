package com.asg.operations.salesquotationprojects.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SalesQuoteProjectsTcDetailResponse {
    private Long transactionPoid;
    private Long detRowId;
    private String clauseRef;
    private String termsDescription;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
}