package com.asg.operations.shipprincipal.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "SHIP_PRINCIPAL_PA_RPT_DTL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ShipPrincipalPaRptDtlId.class)
public class ShipPrincipalPaRptDtl extends BaseEntity {

    @Id
    @Column(name = "PRINCIPAL_POID")
    private Long principalPoid;

    @AuditIgnore
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
}
