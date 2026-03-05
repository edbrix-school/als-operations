package com.asg.operations.portcallreport.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import com.asg.operations.portactivitiesmaster.entity.PortActivityMaster;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "OPS_PORT_CALL_REPORT_DTL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallReportDtlId.class)
public class PortCallReportDtl extends BaseEntity {

    @Id
    @Column(name = "PORT_CALL_REPORT_POID")
    private Long portCallReportPoid;

    @AuditIgnore
    @Id
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Column(name = "PORT_ACTIVITY_TYPE_POID")
    private Long portActivityTypePoid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PORT_ACTIVITY_TYPE_POID", insertable = false, updatable = false)
    private PortActivityMaster portActivityMaster;

    @Column(name = "ACTIVITY_MANDATORY", length = 1)
    private String activityMandatory;

    @PrePersist
    protected void onCreate() {
        if (activityMandatory == null) {
            activityMandatory = "N";
        }
    }
}
