package com.asg.operations.portcalloperation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "OPS_PC_INFO_MAIL_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallOperationMailDtlId.class)
public class PortCallOperationMailDtl {

    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Column(name = "COMMUNICATION_TYPE", length = 100, nullable = false)
    private String communicationType;

    @Column(name = "COMMUNICATION_MODE", length = 100)
    private String communicationMode;

    @Column(name = "COMPANY", length = 300)
    private String company;

    @Column(name = "ADDRESSEE", length = 100)
    private String addressee;

    @Column(name = "EMAIL_IDS", length = 1000)
    private String emailIds;

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
