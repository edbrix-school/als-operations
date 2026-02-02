package com.asg.operations.portcalloperation.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "OPS_PC_OPERATION_HDR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationHdr {
    @AuditIgnore
    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionPoid;
    @AuditIgnore
    @Column(name = "TRANSACTION_DATE", nullable = false)
    private LocalDate transactionDate;
    @AuditIgnore
    @Column(name = "GROUP_POID")
    private Long groupPoid;
    @AuditIgnore
    @Column(name = "DOC_REF", length = 25, unique = true)
    private String docRef;
    @AuditIgnore
    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "VESSEL_VOYAGE_POID")
    private Long vesselVoyagePoid;

    @Column(name = "CALL_SIGN", length = 100)
    private String callSign;

    @Column(name = "CALL_TYPE", length = 100)
    private String callType;

    @Column(name = "PRINCIPAL_POID")
    private Long principalPoid;

    @Column(name = "VESSEL_TYPE_POID")
    private Long vesselTypePoid;
    @Column(name = "OPERATOR_NAME", length = 300)
    private String operatorName;
    @Column(name = "CHARTERER_NAME", length = 300)
    private String chartererName;

    @Column(name = "BERTH", length = 100)
    private String berth;

    @Column(name = "PORT_OF_CALL_POID")
    private Long portOfCallPoid;

    @Column(name = "AGENCY_TYPE", length = 300)
    private String agencyType;

    @Column(name = "SPECIAL_INSTRUCTIONS", length = 4000)
    private String specialInstructions;

    @Column(name = "TERMS_CONDITIONS", length = 4000)
    private String termsConditions;

    @Column(name = "PC_INFO_ATTACHMENTS", length = 4000)
    private String pcInfoAttachments;

    @Column(name = "PDA_REF_POID")
    private Long pdaRefPoid;

    @Column(name = "FDA_REF_POID")
    private Long fdaRefPoid;

    @Column(name = "PDA_ANCHORAGE_STAY_DAYS")
    private BigDecimal pdaAnchorageStayDays;

    @Column(name = "PDA_BERTH_STAY_DAYS")
    private BigDecimal pdaBerthStayDays;

    @Column(name = "PDA_PORT_STAY_DAYS")
    private BigDecimal pdaPortStayDays;

    @Column(name = "PDA_FDA_ATTACHMENTS", length = 4000)
    private String pdaFdaAttachments;

    @Column(name = "PDA_FDA_REMARKS", length = 1000)
    private String pdaFdaRemarks;

    @Column(name = "PORT_CALL_ACTUAL_TIMING_REMARKS", length = 1000)
    private String portCallActualTimingRemarks;
    @AuditIgnore
    @Column(name = "HUSBANDRY_CREW_REQ_BY", length = 100)
    private String husbandryCrewReqBy;

    @Column(name = "DOCS_COPY_EMAIL_POID")
    private Long docsCopyEmailPoid;

    @Column(name = "STATUS", length = 100)
    private String status;
    @AuditIgnore
    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;
    @AuditIgnore
    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
    @AuditIgnore
    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;
    @AuditIgnore
    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;
    @AuditIgnore
    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "GRT")
    private Long grt;

    @Column(name = "NRT")
    private Long nrt;

    @Column(name = "DWT")
    private Long dwt;

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
        if (deleted == null) {
            deleted = "N";
        }
        if (transactionDate == null) {
            transactionDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}
