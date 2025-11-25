package com.alsharif.operations.portcallreport.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "OPS_PORT_CALL_REPORT_HDR")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallReportHdr {

    @Id
    @Column(name = "PORT_CALL_REPORT_POID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long portCallReportPoid;

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

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "REMARKS", length = 1000)
    private String remarks;

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
        if (deleted == null) {
            deleted = "N";
        }
        if (active == null) {
            active = "Y";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}
