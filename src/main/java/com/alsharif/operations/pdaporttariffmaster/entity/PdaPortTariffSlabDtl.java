package com.alsharif.operations.pdaporttariffmaster.entity;

import jakarta.persistence.*;
import com.alsharif.operations.pdaporttariffmaster.key.PdaPortTariffSlabDtlId;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PDA_PORT_TARIFF_SLAB_DTL")
public class PdaPortTariffSlabDtl {

    @EmbeddedId
    private PdaPortTariffSlabDtlId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "TRANSACTION_POID", referencedColumnName = "TRANSACTION_POID", insertable = false, updatable = false),
            @JoinColumn(name = "CHARGE_DET_ROW_ID", referencedColumnName = "DET_ROW_ID", insertable = false, updatable = false)
    })
    private PdaPortTariffChargeDtl chargeDtl;



    // ---------------------------- Fields ----------------------------

    @Column(name = "QUANTITY_FROM", precision = 20, scale = 3)
    private BigDecimal quantityFrom;

    @Column(name = "QUANTITY_TO", precision = 20, scale = 3)
    private BigDecimal quantityTo;

    @Column(name = "DAYS_1")
    private Long days1;

    @Column(name = "RATE_1")
    private BigDecimal rate1;

    @Column(name = "DAYS_2")
    private Long days2;

    @Column(name = "RATE_2")
    private BigDecimal rate2;

    @Column(name = "DAYS_3")
    private Long days3;

    @Column(name = "RATE_3")
    private BigDecimal rate3;

    @Column(name = "DAYS_4")
    private Long days4;

    @Column(name = "RATE_4")
    private BigDecimal rate4;

    @Column(name = "CALL_BY_PORT", length = 1)
    private String callByPort;

    @Column(name = "REMARKS", length = 200)
    private String remarks;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;
}

