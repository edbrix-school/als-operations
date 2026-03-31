package com.asg.operations.pdaRoRoVehicle.entity;


import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
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
public class PdaRoRoEntryHdr extends BaseEntity {

    @AuditIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRANSACTION_POID", nullable = false, updatable = false)
    private Long transactionPoid;

    @Column(name = "TRANSACTION_DATE", nullable = false)
    private LocalDate transactionDate;

    @AuditIgnore
    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @AuditIgnore
    @Column(name = "DOC_REF", updatable = false)
    private String docRef;

    @AuditIgnore
    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "VESSEL_VOYAGE_POID")
    private Long vesselVoyagePoid;

    @AuditIgnore
    @Column(name = "VESSEL_NAME", length = 300)
    private String vesselName;
    @AuditIgnore

    @Column(name = "VOYAGE_NO", length = 100)
    private String voyageNo;

    @Column(name = "REMARKS", length = 2000)
    private String remarks;

    @AuditIgnore
    @Column(name = "DELETED", length = 1)
    private String deleted = "N";
}
