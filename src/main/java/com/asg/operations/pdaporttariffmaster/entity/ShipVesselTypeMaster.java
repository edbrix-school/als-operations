package com.asg.operations.pdaporttariffmaster.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "SHIP_VESSEL_TYPE_MASTER",
        uniqueConstraints = {
                @UniqueConstraint(name = "SHIP_VESSEL_TYPE_MASTER_UKCD", columnNames = {"VESSEL_TYPE_CODE"}),
                @UniqueConstraint(name = "SHIP_VESSEL_TYPE_MASTER_UKNAME", columnNames = {"VESSEL_TYPE_NAME"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipVesselTypeMaster extends BaseEntity {

    @Id
    @Column(name = "VESSEL_TYPE_POID", nullable = false)
    private BigDecimal vesselTypePoid;

    @AuditIgnore
    @Column(name = "GROUP_POID")
    private BigDecimal groupPoid;

    @Column(name = "VESSEL_TYPE_CODE", nullable = false, length = 20)
    private String vesselTypeCode;

    @Column(name = "VESSEL_TYPE_NAME", nullable = false, length = 100)
    private String vesselTypeName;

    @Column(name = "VESSEL_TYPE_NAME2", length = 100)
    private String vesselTypeName2;

    @Column(name = "ACTIVE", length = 1)
    private String active;

    @Column(name = "SEQNO")
    private BigDecimal seqNo;

    @AuditIgnore
    @Column(name = "DELETED", length = 1)
    private String deleted;
}

