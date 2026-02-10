package com.asg.operations.salesquotationprojects.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.operations.salesquotationprojects.key.ShipCommodityMasterId;
import lombok.*;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "SHIP_COMODITY_MASTER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipCommodityMaster {

    @EmbeddedId
    private ShipCommodityMasterId id;

    @Column(name = "COMODITY_CODE", nullable = false, length = 20)
    private String commodityCode;

    @Column(name = "COMODITY_NAME", nullable = false, length = 100)
    private String commodityName;

    @Column(name = "COMODITY_NAME2", length = 100)
    private String commodityName2;

    @Column(name = "ACTIVE", length = 1)
    private String active;

    @Column(name = "SEQNO")
    private BigDecimal seqNo;

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

    @AuditIgnore
    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "COMMODITY_CATEGORY_POID")
    private BigDecimal commodityCategoryPoid;
}
