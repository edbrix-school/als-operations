package com.asg.operations.shipprincipal.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "SHIP_PRINCIPAL_MASTER_DTL")
@IdClass(ShipPrincipalMasterDtlId.class)
public class ShipPrincipalMasterDtl {


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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRINCIPAL_POID", insertable = false, updatable = false)
    private ShipPrincipalMaster principalMaster;

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
