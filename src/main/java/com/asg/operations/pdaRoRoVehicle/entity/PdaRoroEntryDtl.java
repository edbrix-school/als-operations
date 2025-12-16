package com.asg.operations.pdaRoRoVehicle.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PDA_RORO_ENTRY_DTL")
public class PdaRoroEntryDtl {

    @EmbeddedId
    private PdaRoroEntryDtlId id;

    @MapsId("transactionPoid")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TRANSACTION_POID", nullable = false, updatable = false)
    private PdaRoroEntryHdr header;

    @Column(name = "BL_NUMBER", length = 300)
    private String blNumber;

    @Column(name = "SHIPPER", length = 500)
    private String shipper;

    @Column(name = "CONSIGNEE", length = 500)
    private String consignee;

    @Column(name = "VIN_NUMBER", length = 500)
    private String vinNumber;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "BL_GWT")
    private Double blGwt;

    @Column(name = "BL_CBM")
    private Double blCbm;

    @Column(name = "PORT_OF_LOAD", length = 500)
    private String portOfLoad;

    @Column(name = "AGENT", length = 500)
    private String agent;

    @Column(name = "REMARKS", length = 2000)
    private String remarks;

    @Column(name = "CREATED_BY", length = 20, updatable = false)
    private String createdBy;

    @Column(name = "CREATED_DATE", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;
}
