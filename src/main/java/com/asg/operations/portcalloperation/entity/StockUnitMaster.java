package com.asg.operations.portcalloperation.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "STOCK_UNIT_MASTER",
    uniqueConstraints = {
        @UniqueConstraint(name = "STOCK_UNIT_MASTER_UK1", columnNames = "STOCK_UNIT_CODE"),
        @UniqueConstraint(name = "STOCK_UNIT_MASTER_UK2", columnNames = "STOCK_UNIT_NAME")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class StockUnitMaster {

    @Id
    @Column(name = "STOCK_UNIT_POID", nullable = false)
    private Long stockUnitPoid;

    @Column(name = "STOCK_UNIT_CODE", length = 20)
    private String stockUnitCode;

    @Column(name = "STOCK_UNIT_NAME", length = 100)
    private String stockUnitName;

    @Column(name = "STOCK_UNIT_NAME2", length = 100)
    private String stockUnitName2;

    @AuditIgnore
    @Column(name = "GROUP_POID")
    private Long groupPoid;
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

    @Column(name = "ACTIVE", length = 1)
    private String active;

    @Column(name = "SEQNO")
    private Integer seqNo;

    @AuditIgnore
    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "CLASSIFIED", length = 20)
    private String classified;
}
