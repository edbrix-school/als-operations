package com.alsharif.operations.pdaporttariffmaster.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(
        name = "PDA_RATE_TYPE_MASTER",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"RATE_TYPE_POID"}),   // PK
                @UniqueConstraint(columnNames = {"RATE_TYPE_POID", "GROUP_POID"}) // secondary unique index
        }
)
public class PdaRateTypeMaster {

    @Id
    @Column(name = "RATE_TYPE_POID", nullable = false)
    private Long rateTypePoid;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "RATE_TYPE_CODE", nullable = false, length = 20)
    private String rateTypeCode;

    @Column(name = "RATE_TYPE_NAME", nullable = false, length = 100)
    private String rateTypeName;

    @Column(name = "RATE_TYPE_NAME2", length = 100)
    private String rateTypeName2;

    @Column(name = "RATE_TYPE_FORMULA", length = 1000)
    private String rateTypeFormula;

    @Column(name = "ACTIVE", length = 1)
    private String active;

    @Column(name = "SEQNO", precision = 25)
    private BigDecimal seqNo;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private Timestamp createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private Timestamp lastModifiedDate;

    @Column(name = "DEF_QTY", length = 100)
    private String defQty;

    @Column(name = "DEF_DAYS")
    private BigDecimal defDays;

    @Column(name = "DELETED", length = 1)
    private String deleted;
}
