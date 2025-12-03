package com.asg.operations.pdaporttariffmaster.entity;

import com.asg.operations.pdaporttariffmaster.key.ShipPortMasterId;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "SHIP_PORT_MASTER",
        uniqueConstraints = {
                @UniqueConstraint(name = "SHIP_PORT_MASTER_UK_PORTCODE", columnNames = {"PORT_CODE"}),
                @UniqueConstraint(name = "SHIP_PORT_MASTER_UK_PORTNAME", columnNames = {"PORT_NAME"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipPortMaster {

    @EmbeddedId
    private ShipPortMasterId id;

    @Column(name = "PORT_CODE", nullable = false, length = 20)
    private String portCode;

    @Column(name = "PORT_NAME", nullable = false, length = 100)
    private String portName;

    @Column(name = "PORT_NAME2", length = 100)
    private String portName2;

    @Column(name = "COUNTRY_POID", nullable = false)
    private BigDecimal countryPoid;

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

    @Column(name = "TRADELANE_POID", nullable = false)
    private BigDecimal tradelanePoid;

    @Column(name = "GLOBAL_PORT_CODE", length = 20)
    private String globalPortCode;

    @Column(name = "DELETED", length = 1)
    private String deleted;
}

