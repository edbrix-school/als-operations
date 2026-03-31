package com.asg.operations.portcalloperation.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "OPS_PC_ACT_COND_DTL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallOperationActCondDtlId.class)
public class PortCallOperationActCondDtl extends BaseEntity {

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
}
