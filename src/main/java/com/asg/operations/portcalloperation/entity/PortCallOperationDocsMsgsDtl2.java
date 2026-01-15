package com.asg.operations.portcalloperation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "OPS_PC_DOCS_MSGS_DTL2")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallOperationDocsMsgsDtl2Id.class)
public class PortCallOperationDocsMsgsDtl2 {

    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

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

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

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
