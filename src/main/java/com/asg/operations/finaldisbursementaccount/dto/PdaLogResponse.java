package com.asg.operations.finaldisbursementaccount.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class PdaLogResponse {

    private Long pdaTransactionPoid;
    private String pdaDocRef;
    private LocalDate pdaTransactionDate;

    private Long fdaTransactionPoid;
    private String fdaDocRef;
    private LocalDate fdaTransactionDate;
    private String fdaStatus;

    private LocalDate documentSubmittedDate;
    private String documentSubmittedBy;
    private String documentSubmittedStatus;

    private LocalDate verificationAcceptedDate;
    private String verificationAcceptedBy;
    private String documentReceivedStatus;

    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
}

