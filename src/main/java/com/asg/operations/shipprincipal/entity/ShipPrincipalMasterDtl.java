package com.asg.operations.shipprincipal.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "SHIP_PRINCIPAL_MASTER_DTL")
@IdClass(ShipPrincipalMasterDtlId.class)
public class ShipPrincipalMasterDtl extends BaseEntity {

    @Id
    @Column(name = "PRINCIPAL_POID", nullable = false)
    private Long principalPoid;

    @AuditIgnore
    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    private Long detRowId;

    @Column(name = "CHARGE_POID")
    private Long chargePoid;

    @Column(name = "RATE")
    private Long rate;

    @Column(name = "REMARKS", length = 100)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRINCIPAL_POID", insertable = false, updatable = false)
    private ShipPrincipalMaster principalMaster;
}
