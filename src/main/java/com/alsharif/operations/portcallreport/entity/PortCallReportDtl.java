package com.alsharif.operations.portcallreport.entity;

import com.alsharif.operations.portactivitiesmaster.entity.PortActivityMaster;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "OPS_PORT_CALL_REPORT_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallReportDtlId.class)
public class PortCallReportDtl {

    @Id
    @Column(name = "PORT_CALL_REPORT_POID")
    private Long portCallReportPoid;

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
        if (activityMandatory == null) {
            activityMandatory = "N";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}
