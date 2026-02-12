package com.asg.operations.salesquotationprojects.key;

import com.asg.common.lib.annotation.AuditIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesQuoteProjectsNotesDtlId implements Serializable {

    @AuditIgnore
    @Column(name = "TRANSACTION_POID", nullable = false)
    private Long transactionPoid;

    @AuditIgnore
    @Column(name = "DET_ROW_ID", nullable = false)
    private Long detRowId;
}
