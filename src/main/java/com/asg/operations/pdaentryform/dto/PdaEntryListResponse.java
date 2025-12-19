package com.asg.operations.pdaentryform.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdaEntryListResponse {

    @JsonProperty("TRANSACTION_POID")
    private Long transactionPoid;

    @JsonProperty("DOC_REF")
    private String docRef;

    @JsonProperty("TRANSACTION_REF")
    private String transactionRef;

    @JsonProperty("TRANSACTION_DATE")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    @JsonProperty("PRINCIPAL_POID")
    private BigDecimal principalPoid;

    @JsonProperty("PRINCIPAL_NAME")
    private String principalName;

    @JsonProperty("VOYAGE_POID")
    private BigDecimal voyagePoid;

    @JsonProperty("VOYAGE_NO")
    private String voyageNo;

    @JsonProperty("VESSEL_POID")
    private BigDecimal vesselPoid;

    @JsonProperty("VESSEL_NAME")
    private String vesselName;

    @JsonProperty("PORT_POID")
    private BigDecimal portPoid;

    @JsonProperty("PORT_DESCRIPTION")
    private String portDescription;

    @JsonProperty("STATUS")
    private String status;

    @JsonProperty("REF_TYPE")
    private String refType;

    @JsonProperty("FDA_REF")
    private String fdaRef;

    @JsonProperty("TOTAL_AMOUNT")
    private BigDecimal totalAmount;

    @JsonProperty("CURRENCY_CODE")
    private String currencyCode;

    @JsonProperty("DELETED")
    private String deleted;

    @JsonProperty("CREATED_BY")
    private String createdBy;

    @JsonProperty("CREATED_DATE")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;

    @JsonProperty("LASTMODIFIED_BY")
    private String lastModifiedBy;

    @JsonProperty("LASTMODIFIED_DATE")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastModifiedDate;
}