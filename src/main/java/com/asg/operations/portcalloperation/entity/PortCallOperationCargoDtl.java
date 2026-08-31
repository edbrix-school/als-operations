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
@Table(name = "OPS_PC_INFO_CARGO_DTL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallOperationCargoDtlId.class)
public class PortCallOperationCargoDtl extends BaseEntity {

    @AuditIgnore
    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @AuditIgnore
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

    @Column(name = "UOM", length = 50)
    private String uom;

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
}
