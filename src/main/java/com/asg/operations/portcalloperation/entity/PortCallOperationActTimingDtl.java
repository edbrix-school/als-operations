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
@Table(name = "OPS_PC_ACT_TIMING_DTL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallOperationActTimingDtlId.class)
public class PortCallOperationActTimingDtl extends BaseEntity {

    @AuditIgnore
    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @AuditIgnore
    @Id
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Column(name = "PORT_REPORT_POID")
    private Long portReportPoid;

    @Column(name = "ACTUALS_TIMING_DTL_POID")
    private Long actualsTimingDtlPoid;

    @Column(name = "EMAIL_POID")
    private Long emailPoid;

    @Column(name = "TIMING_ATTACHMENTS", length = 4000)
    private String timingAttachments;
}
