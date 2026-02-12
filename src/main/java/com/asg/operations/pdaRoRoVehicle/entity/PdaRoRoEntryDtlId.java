package com.asg.operations.pdaRoRoVehicle.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class PdaRoRoEntryDtlId implements Serializable {

    @AuditIgnore
    @Column(name = "TRANSACTION_POID", nullable = false)
    private Long transactionPoid;

    @AuditIgnore
    @Column(name = "DET_ROW_ID", nullable = false)
    private Long detRowId;
}
