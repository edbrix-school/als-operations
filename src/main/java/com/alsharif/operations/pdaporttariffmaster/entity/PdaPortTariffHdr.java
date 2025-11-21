package com.alsharif.operations.pdaporttariffmaster.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "PDA_PORT_TARIFF_HDR")
public class PdaPortTariffHdr {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pda_port_tariff_hdr_seq")
    @SequenceGenerator(name = "pda_port_tariff_hdr_seq", sequenceName = "PDA_PORT_TARIFF_HDR_SEQ", allocationSize = 1)
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @Column(name = "TRANSACTION_DATE")
    private LocalDate transactionDate;

    @Column(name = "GROUP_POID", nullable = false)
    @NotNull
    private BigDecimal groupPoid;

    @Column(name = "COMPANY_POID")
    private BigDecimal companyPoid;

    @Column(name = "DOC_REF", unique = true, length = 25)
    @Size(max = 25)
    private String docRef;

    @Column(name = "PORTS", length = 200)
    @Size(max = 200)
    private String ports; // Comma-separated port POIDs

    @Column(name = "VESSEL_TYPES", length = 200)
    @Size(max = 200)
    private String vesselTypes; // Comma-separated vessel type POIDs

    @Column(name = "PERIOD_FROM", nullable = false)
    @NotNull
    private LocalDate periodFrom;

    @Column(name = "PERIOD_TO", nullable = false)
    @NotNull
    private LocalDate periodTo;

    @Column(name = "REMARKS", length = 500)
    @Size(max = 500)
    private String remarks;

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
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @OneToMany(mappedBy = "tariffHdr", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PdaPortTariffChargeDtl> chargeDetails;

}