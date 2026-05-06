package com.asg.operations.portcalloperation.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "OPS_PC_DOCS_MSGS_DTL1")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallOperationDocsMsgsDtl1Id.class)
public class PortCallOperationDocsMsgsDtl1 extends BaseEntity {

    @AuditIgnore
    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @AuditIgnore
    @Id
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "email_poid_seq")
    @SequenceGenerator(name = "email_poid_seq", sequenceName = "OPS_PC_DOCS_MSGS_DTL1_SEQ", allocationSize = 1)
    @Column(name = "EMAIL_POID")
    private Long emailPoid;


    @Column(name = "SEND_BY_POID")
    private Long sendByPoid;

    @Column(name = "EMAIL_SUBJECT", length = 1000)
    private String emailSubject;

    @Column(name = "EMAIL_DOCUMENTS", length = 4000)
    private String emailDocuments;

    @Column(name = "EMAIL_SEND_ON")
    private LocalDate emailSendOn;

    @Column(name = "EMAIL_CONTENT", length = 4000)
    private String emailContent;

    @Column(name = "EMAIL_REMARKS", length = 1000)
    private String emailRemarks;
}
