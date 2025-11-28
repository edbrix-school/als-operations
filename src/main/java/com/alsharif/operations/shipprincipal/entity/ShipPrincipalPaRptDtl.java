package com.alsharif.operations.shipprincipal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "SHIP_PRINCIPAL_PA_RPT_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ShipPrincipalPaRptDtlId.class)
public class ShipPrincipalPaRptDtl {

    @Id
    @Column(name = "PRINCIPAL_POID")
    private Long principalPoid;

    @Id
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Column(name = "SN")
    private Long sn;

    @Column(name = "PORT_CALL_REPORT_TYPE")
    private Long portCallReportType;

    @Column(name = "PDF_TEMPLATE_POID")
    private Long pdfTemplatePoid;

    @Column(name = "EMAIL_TEMPLATE_POID")
    private Long emailTemplatePoid;

    @Column(name = "ASSIGNED_TO_ROLE_POID")
    private Long assignedToRolePoid;

    @Column(name = "VESSEL_TYPE", length = 300)
    private String vesselType;

    @Column(name = "RESPONSE_TIME_HRS")
    private Long responseTimeHrs;

    @Column(name = "FREQUENCE_HRS")
    private Long frequenceHrs;

    @Column(name = "ESCALATION_ROLE1")
    private Long escalationRole1;

    @Column(name = "ESCALATION_ROLE2")
    private Long escalationRole2;

    @Column(name = "REMARKS", length = 100)
    private String remarks;

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
