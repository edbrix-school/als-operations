package com.asg.operations.portcalloperation.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "OPS_PC_INFO_CARGO_DTL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallOperationCargoDtlId.class)
public class PortCallOperationCargoDtl {

    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Column(name = "PRODUCT_NAME", length = 300)
    private String productName;

    @Column(name = "PORT_CARGO_NAME", length = 300)
    private String portCargoName;

    @Column(name = "QTY_MT")
    private BigDecimal qtyMt;

    @Column(name = "QTY_CBM")
    private BigDecimal qtyCbm;

    @Column(name = "NO_OF_QTY")
    private BigDecimal noOfQty;

    @Column(name = "CALL_TYPE", length = 100)
    private String callType;

    @Column(name = "PORT_OF_CALL_POID")
    private Long portOfCallPoid;

    @Column(name = "BERTH", length = 100)
    private String berth;

    @Column(name = "SHIPPER", length = 300)
    private String shipper;

    @Column(name = "RECEIVER", length = 300)
    private String receiver;
    @AuditIgnore
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
