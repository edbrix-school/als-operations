package com.asg.operations.finaldisbursementaccount.dto;

import com.asg.operations.commonlov.dto.LovItem;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class FdaListResponse {

    @JsonProperty("TRANSACTION_POID")
    private Long transactionPoid;

    @JsonProperty("TRANSACTION_DATE")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    @JsonProperty("GROUP_POID")
    private Long groupPoid;

    @JsonProperty("COMPANY_POID")
    private Long companyPoid;

    @JsonProperty("PRINCIPAL_POID")
    private Long principalPoid;

    @JsonProperty("PRINCIPAL_NAME")
    private String principalName;

    @JsonProperty("PRINCIPAL_CONTACT")
    private String principalContact;

    @JsonProperty("DOC_REF")
    private String docRef;

    @JsonProperty("VOYAGE_POID")
    private Long voyagePoid;

    @JsonProperty("VESSEL_POID")
    private Long vesselPoid;

    @JsonProperty("VESSEL_NAME")
    private String vesselName;

    @JsonProperty("ARRIVAL_DATE")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate arrivalDate;

    @JsonProperty("SAIL_DATE")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate sailDate;

    @JsonProperty("PORT_POID")
    private Long portPoid;

    @JsonProperty("COMMODITY_POID")
    private String commodityPoid;

    @JsonProperty("OPERATION_TYPE")
    private String operationType;

    @JsonProperty("TOTAL_QUANTITY")
    private BigDecimal totalQuantity;

    @JsonProperty("UNIT")
    private String unit;

    @JsonProperty("HARBOUR_CALL_TYPE")
    private String harbourCallType;

    @JsonProperty("CURRENCY_CODE")
    private String currencyCode;

    @JsonProperty("STATUS")
    private String status;

    @JsonProperty("TOTAL_AMOUNT")
    private BigDecimal totalAmount;

    @JsonProperty("PDA_REF")
    private String pdaRef;

    @JsonProperty("SALESMAN_POID")
    private Long salesmanPoid;

    @JsonProperty("VOYAGE_NO")
    private String voyageNo;

    @JsonProperty("REF_TYPE")
    private String refType;

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