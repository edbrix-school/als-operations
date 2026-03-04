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
@Table(name = "OPS_PC_EST_PREARRIVAL_ACT_DTL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallOperationEstPrearrivalActDtlId.class)
public class PortCallOperationEstPrearrivalActDtl extends BaseEntity {

    @AuditIgnore
    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @AuditIgnore
    @Id
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Id
    @Column(name = "PRE_ACTIVITY_DTL_POID")
    private Long preActivityDtlPoid;

    @Column(name = "ACTIVITY_POID")
    private Long activityPoid;

    @Column(name = "OTHER_DESCRIPTION", length = 300)
    private String otherDescription;

    @Column(name = "ESTIMATED_DATETIME")
    private LocalDateTime estimatedDatetime;
}
