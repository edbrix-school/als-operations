package com.asg.operations.portcalloperation.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "OPS_PC_DOCS_MSGS_DTL2")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallOperationDocsMsgsDtl2Id.class)
public class PortCallOperationDocsMsgsDtl2 extends BaseEntity {

    @AuditIgnore
    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @AuditIgnore
    @Id
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Column(name = "EMAIL_POID")
    private Long emailPoid;

    @Column(name = "EMAIL_TYPE", length = 300)
    private String emailType;

    @Column(name = "COMPANY", length = 300)
    private String company;

    @Column(name = "ADDRESSEE", length = 300)
    private String addressee;

    @Column(name = "TO_EMAIL_ID", length = 1000)
    private String toEmailId;

    @Column(name = "CC_EMAIL_ID", length = 1000)
    private String ccEmailId;
}
