package com.asg.operations.pdaporttariffmaster.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
public class ShipVesselTypeMaster {

    @Id
    @Column(name = "VESSEL_TYPE_POID", nullable = false)
    private BigDecimal vesselTypePoid;

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

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @Column(name = "DELETED", length = 1)
    private String deleted;
}

