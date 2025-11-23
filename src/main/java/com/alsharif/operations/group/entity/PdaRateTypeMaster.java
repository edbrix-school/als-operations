package com.alsharif.operations.group.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Table(name = "PDA_RATE_TYPE_MASTER")
@Data
@Builder
@AllArgsConstructor
public class PdaRateTypeMaster {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RATE_TYPE_POID")
    private Long rateTypePoid;

    @Column(name = "GROUP_POID", nullable = false)
    private BigDecimal groupPoid;

    @Column(name = "RATE_TYPE_CODE", length = 20, nullable = false)
    @NotBlank
    @Size(max = 20)
    private String rateTypeCode;

    @Column(name = "RATE_TYPE_NAME", length = 100, nullable = false)
    @NotBlank
    @Size(max = 100)
    private String rateTypeName;

    @Column(name = "RATE_TYPE_NAME2", length = 100)
    @Size(max = 100)
    private String rateTypeName2;

    @Column(name = "RATE_TYPE_FORMULA", length = 1000)
    @Size(max = 1000)
    private String rateTypeFormula;

    @Column(name = "DEF_QTY", length = 100)
    @Size(max = 100)
    private String defQty;

    @Column(name = "DEF_DAYS")
    private BigDecimal defDays;

    @Column(name = "SEQNO")
    private BigInteger seqno;

    @Column(name = "ACTIVE", length = 1)
    @Size(max = 1)
    private String active;

    @Column(name = "DELETED", length = 1)
    @Size(max = 1)
    private String deleted;

    @Column(name = "CREATED_BY", length = 20)
    @Size(max = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    @Size(max = 20)
    private String lastmodifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastmodifiedDate;

    // Constructors
    public PdaRateTypeMaster() {
    }

    public void setRateTypePoid(Long rateTypePoid) {
        this.rateTypePoid = rateTypePoid;
    }

    public void setGroupPoid(BigDecimal groupPoid) {
        this.groupPoid = groupPoid;
    }

    public void setRateTypeCode(String rateTypeCode) {
        this.rateTypeCode = rateTypeCode;
    }

    public void setRateTypeName(String rateTypeName) {
        this.rateTypeName = rateTypeName;
    }

    public void setRateTypeName2(String rateTypeName2) {
        this.rateTypeName2 = rateTypeName2;
    }

    public void setRateTypeFormula(String rateTypeFormula) {
        this.rateTypeFormula = rateTypeFormula;
    }

    public void setDefQty(String defQty) {
        this.defQty = defQty;
    }

    public void setDefDays(BigDecimal defDays) {
        this.defDays = defDays;
    }

    public void setSeqno(BigInteger seqno) {
        this.seqno = seqno;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public void setLastmodifiedBy(String lastmodifiedBy) {
        this.lastmodifiedBy = lastmodifiedBy;
    }

    public void setLastmodifiedDate(LocalDateTime lastmodifiedDate) {
        this.lastmodifiedDate = lastmodifiedDate;
    }
}
