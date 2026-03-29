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
@Table(name = "OPS_PC_ACT_TIMINGS_ACTVTY_DTL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallOperationActTimingsActvtyDtlId.class)
public class PortCallOperationActTimingsActvtyDtl extends BaseEntity {

    @AuditIgnore
    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @AuditIgnore
    @Id
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Id
    @Column(name = "ACTUALS_TIMING_DTL_POID")
    private Long actualsTimingDtlPoid;

    @Column(name = "ACTIVITY_POID")
    private Long activityPoid;

    @Column(name = "ACTIVITY_NAME", length = 300)
    private String activityName;

    @Column(name = "DETAILS", length = 300)
    private String details;

    @Column(name = "ESTIMATED_DATETIME")
    private LocalDateTime estimatedDatetime;
}
