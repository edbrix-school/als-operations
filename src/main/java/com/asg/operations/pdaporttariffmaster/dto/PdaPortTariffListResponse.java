package com.asg.operations.pdaporttariffmaster.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class PdaPortTariffListResponse {
    @JsonProperty("TRANSACTION_POID")
    private Long transactionPoid;

    @JsonProperty("TRANSACTION_DATE")
    private LocalDate transactionDate;

    @JsonProperty("DOC_REF")
    private String docRef;

    @JsonProperty("PORTS")
    private String port;

    @JsonProperty("PORT_NAME")
    private String portName;

    @JsonProperty("PERIOD_FROM")
    private LocalDate periodFrom;

    @JsonProperty("PERIOD_TO")
    private LocalDate periodTo;

    @JsonProperty("REMARKS")
    private String remarks;

    @JsonProperty("DELETED")
    private String deleted;

    @JsonProperty("CREATED_BY")
    private String createdBy;

    @JsonProperty("CREATED_DATE")
    private LocalDateTime createdDate;

    @JsonProperty("LAST_MODIFIED_BY")
    private String lastModifiedBy;

    @JsonProperty("LAST_MODIFIED_DATE")
    private LocalDateTime lastModifiedDate;
}