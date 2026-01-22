package com.asg.operations.portcalloperation.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "OPS_PC_DOCS_MSGS_DTL1")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallOperationDocsMsgsDtl1Id.class)
public class PortCallOperationDocsMsgsDtl1 {
    @AuditIgnore
    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;
    @AuditIgnore
    @Id
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Id
    @Column(name = "EMAIL_POID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ops_pc_docs_msgs_dtl1_seq")
    @SequenceGenerator(name = "ops_pc_docs_msgs_dtl1_seq", sequenceName = "OPS_PC_DOCS_MSGS_DTL1_SEQ", allocationSize = 1)
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
    @AuditIgnore
    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;
    @AuditIgnore
    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
    @AuditIgnore
    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;
@AuditIgnore
    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}
