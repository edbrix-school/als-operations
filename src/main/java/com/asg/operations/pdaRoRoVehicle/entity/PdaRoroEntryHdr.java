package com.asg.operations.pdaRoRoVehicle.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "PDA_RORO_ENTRY_HDR",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "PDA_RORO_ENTRY_HDR_HDR_UK",
                        columnNames = "DOC_REF"
                )
        }
)
public class PdaRoroEntryHdr {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "PDA_RORO_ENTRY_HDR_SEQ_GEN"
    )
    @SequenceGenerator(
            name = "PDA_RORO_ENTRY_HDR_SEQ_GEN",
            sequenceName = "PDA_RORO_ENTRY_HDR_SEQ",
            allocationSize = 1
    )
    @Column(name = "TRANSACTION_POID", nullable = false, updatable = false)
    private Long transactionPoid;

    @Column(name = "TRANSACTION_DATE", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "DOC_REF", updatable = false)
    private String docRef;

    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "VESSEL_VOYAGE_POID")
    private Long vesselVoyagePoid;

    @Column(name = "VESSEL_NAME", length = 300)
    private String vesselName;

    @Column(name = "VOYAGE_NO", length = 100)
    private String voyageNo;

    @Column(name = "REMARKS", length = 2000)
    private String remarks;

    @Column(name = "CREATED_BY", length = 30, updatable = false)
    private String createdBy;

    @Column(name = "CREATED_DATE", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 30)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @Column(name = "DELETED", length = 1)
    private String deleted = "N";
}
