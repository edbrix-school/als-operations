package com.asg.operations.pdaentryform.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "PDA_ENTRY_ACKNOWLEDGMENT_DTL")
@IdClass(PdaEntryAcknowledgmentDtlId.class)
public class PdaEntryAcknowledgmentDtl extends BaseEntity {

    @AuditIgnore
    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    @NotNull
    private Long transactionPoid;

    @AuditIgnore
    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    @NotNull
    private Long detRowId;

    @Column(name = "PARTICULARS", length = 2000)
    @Size(max = 2000)
    private String particulars;

    @Column(name = "SELECTED", length = 1)
    @Size(max = 1)
    private String selected;

    @Column(name = "REMARKS", length = 4000)
    @Size(max = 4000)
    private String remarks;

}

