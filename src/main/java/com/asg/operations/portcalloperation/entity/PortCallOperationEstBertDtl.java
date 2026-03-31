package com.asg.operations.portcalloperation.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "OPS_PC_EST_BERT_DTL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallOperationEstBertDtlId.class)
public class PortCallOperationEstBertDtl extends BaseEntity {

    @AuditIgnore
    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @AuditIgnore
    @Id
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Column(name = "ETA")
    private LocalDateTime eta;

    @Column(name = "ETB")
    private LocalDateTime etb;

    @Column(name = "BERTHING_ATTACHMENTS", length = 4000)
    private String berthingAttachments;

    @Column(name = "EMAIL_POID")
    private Long emailPoid;
}
