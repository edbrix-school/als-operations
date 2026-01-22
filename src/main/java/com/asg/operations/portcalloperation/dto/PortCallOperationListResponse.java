package com.asg.operations.portcalloperation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PortCallOperationListResponse {
    @JsonProperty("TRANSACTION_POID")
    private Long transactionPoid;

    @JsonProperty("DOC_REF")
    private String docRef;

    @JsonProperty("TRANSACTION_DATE")
    private LocalDate transactionDate;

    @JsonProperty("STATUS")
    private String status;

    @JsonProperty("CALL_SIGN")
    private String callSign;

    @JsonProperty("CALL_TYPE")
    private String callType;

    @JsonProperty("PORT_OF_CALL_POID")
    private Long portOfCallPoid;
}
