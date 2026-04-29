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
@Table(name = "OPS_PC_INFO_MAIL_DTL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallOperationMailDtlId.class)
public class PortCallOperationMailDtl extends BaseEntity {

    @AuditIgnore
    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @AuditIgnore
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

    @Column(name = "EMAIL_IDS_CC", length = 1000)
    private String emailIdsCC;
}
