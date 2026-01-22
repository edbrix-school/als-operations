package com.asg.operations.portcalloperation.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "OPS_PC_ACT_COND_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallOperationActCondDtlId.class)
public class PortCallOperationActCondDtl {
   @AuditIgnore
    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;
    @AuditIgnore
    @Id
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Column(name = "CONDITION_TYPE", length = 100)
    private String conditionType;

    @Column(name = "DRAFT_FORWARD")
    private BigDecimal draftForward;

    @Column(name = "DRAFT_MID")
    private BigDecimal draftMid;

    @Column(name = "DRAFT_AFT")
    private BigDecimal draftAft;

    @Column(name = "FUEL_OIL")
    private BigDecimal fuelOil;

    @Column(name = "DIESEL_OIL")
    private BigDecimal dieselOil;

    @Column(name = "FRESH_WATER")
    private BigDecimal freshWater;

    @Column(name = "TUGS_SERVICE")
    private BigDecimal tugsService;
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
