package com.alsharif.operations.pdaentryform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "PDA_ENTRY_DTL")
@IdClass(PdaEntryDtlId.class)
public class PdaEntryDtl {

    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    @NotNull
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    @NotNull
    private Long detRowId;

    @Column(name = "CHARGE_POID", nullable = false)
    @NotNull
    private BigDecimal chargePoid;

    @Column(name = "RATE_TYPE_POID")
    private BigDecimal rateTypePoid;

    @Column(name = "PRINCIPAL_POID")
    private BigDecimal principalPoid;

    @Column(name = "CURRENCY_CODE", length = 20)
    @Size(max = 20)
    private String currencyCode;

    @Column(name = "CURRENCY_RATE")
    private BigDecimal currencyRate;

    @Column(name = "QTY", nullable = false)
    @NotNull
    private BigDecimal qty;

    @Column(name = "DAYS", nullable = false)
    @NotNull
    private BigDecimal days;

    @Column(name = "PDA_RATE", nullable = false)
    @NotNull
    private BigDecimal pdaRate;

    @Column(name = "TAX_POID")
    private BigDecimal taxPoid;

    @Column(name = "TAX_PERCENTAGE")
    private BigDecimal taxPercentage;

    @Column(name = "TAX_AMOUNT")
    private BigDecimal taxAmount;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    @Column(name = "FDA_AMOUNT")
    private BigDecimal fdaAmount;

    @Column(name = "FDA_DOC_REF", length = 100)
    @Size(max = 100)
    private String fdaDocRef;

    @Column(name = "FDA_POID")
    private BigDecimal fdaPoid;

    @Column(name = "FDA_CREATION_TYPE", length = 100)
    @Size(max = 100)
    private String fdaCreationType;

    @Column(name = "DATA_SOURCE", length = 20)
    @Size(max = 20)
    private String dataSource;

    @Column(name = "DETAIL_FROM", length = 100)
    @Size(max = 100)
    private String detailFrom;

    @Column(name = "MANUAL", length = 1)
    @Size(max = 1)
    private String manual;

    @Column(name = "SEQNO")
    private Integer seqno;

    @Column(name = "REMARKS", length = 500)
    @Size(max = 500)
    private String remarks;

    @Column(name = "OLD_CHARGE_CODE", length = 100)
    @Size(max = 100)
    private String oldChargeCode;

    @Column(name = "CREATED_BY", length = 20)
    @Size(max = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    @Size(max = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSACTION_POID", insertable = false, updatable = false)
    private PdaEntryHdr entryHdr;

    // Getters and Setters

}

