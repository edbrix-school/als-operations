package com.asg.operations.portcalloperation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "OPS_PC_ACT_BUNKER_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallOperationActBunkerDtlId.class)
public class PortCallOperationActBunkerDtl {

    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

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
