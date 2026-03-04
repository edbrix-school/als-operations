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
@Table(name = "OPS_PC_ACT_BUNKER_DTL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallOperationActBunkerDtlId.class)
public class PortCallOperationActBunkerDtl extends BaseEntity {

    @AuditIgnore
    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @AuditIgnore
    @Id
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Column(name = "GRADE", length = 100)
    private String grade;

    @Column(name = "NOMINATTED_QTY_MT")
    private BigDecimal nominatedQtyMt;

    @Column(name = "SUPPLIED_QTY_MT")
    private BigDecimal suppliedQtyMt;

    @Column(name = "SHIP_QTY_MT")
    private BigDecimal shipQtyMt;
}
