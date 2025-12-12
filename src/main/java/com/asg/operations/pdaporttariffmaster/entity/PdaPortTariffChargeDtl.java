package com.asg.operations.pdaporttariffmaster.entity;

import com.asg.operations.pdaporttariffmaster.key.PdaPortTariffChargeDtlId;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PDA_PORT_TARIFF_CHARGE_DTL")
public class PdaPortTariffChargeDtl {

    @EmbeddedId
    private PdaPortTariffChargeDtlId id;

    @Column(name = "CHARGE_POID")
    private Long chargePoid;

    @Column(name = "RATE_TYPE_POID")
    private Long rateTypePoid;

    @Column(name = "TARIFF_SLAB", length = 20)
    @Size(max = 20)
    private String tariffSlab;

    @Column(name = "FIX_RATE")
    private BigDecimal fixRate;

    @Column(name = "HARBOR_CALL_TYPE", length = 50)
    @Size(max = 50)
    private String harborCallType;

    @Column(name = "IS_ENABLED", length = 1)
    @Size(max = 1)
    private String isEnabled;

    @Column(name = "REMARKS", length = 300)
    @Size(max = 300)
    private String remarks;

    @Column(name = "SEQNO")
    private Integer seqNo;

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

    @MapsId("transactionPoid")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSACTION_POID", nullable = false)
    private PdaPortTariffHdr tariffHdr;

    @OneToMany(mappedBy = "chargeDtl", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PdaPortTariffSlabDtl> slabDetails;
}
