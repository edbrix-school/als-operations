package com.asg.operations.portcallreport.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "OPS_PORT_CALL_REPORT_HDR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallReportHdr extends BaseEntity {

    @Id
    @Column(name = "PORT_CALL_REPORT_POID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long portCallReportPoid;

    @AuditIgnore
    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "PORT_CALL_REPORT_ID", length = 50, nullable = false)
    private String portCallReportId;

    @Column(name = "PORT_CALL_REPORT_NAME", length = 300, nullable = false)
    private String portCallReportName;

    @Column(name = "PORT_CALL_APPL_VESSEL_TYPE", length = 300)
    private String portCallApplVesselType;

    @Column(name = "ACTIVE", length = 1)
    private String active;

    @Column(name = "SEQNO")
    private Long seqno;

    @AuditIgnore
    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "REMARKS", length = 1000)
    private String remarks;

    @PrePersist
    protected void onCreate() {
        if (deleted == null) {
            deleted = "N";
        }
        if (active == null) {
            active = "Y";
        }
    }
}
