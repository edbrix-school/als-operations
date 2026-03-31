package com.asg.operations.pdaratetypemaster.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.math.BigInteger;

@Entity
@Table(name = "PDA_RATE_TYPE_MASTER")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdaRateTypeMaster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RATE_TYPE_POID")
    private Long rateTypePoid;

    @AuditIgnore
    @Column(name = "GROUP_POID", nullable = false)
    private Long groupPoid;

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
    private Long defDays;

    @Column(name = "SEQNO")
    private BigInteger seqno;

    @Column(name = "ACTIVE", length = 1)
    @Size(max = 1)
    private String active;

    @AuditIgnore
    @Column(name = "DELETED", length = 1)
    @Size(max = 1)
    private String deleted;
}
