package com.asg.operations.pdaRoRoVehicle.entity;


import com.asg.common.lib.annotation.AuditIgnore;
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
public class PdaRoRoEntryHdr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRANSACTION_POID", nullable = false, updatable = false)
    private Long transactionPoid;
   @AuditIgnore
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
    @Column(name = "CREATED_BY", length = 30, updatable = false)
    private String createdBy;
    @AuditIgnore
    @Column(name = "CREATED_DATE", updatable = false)
    private LocalDateTime createdDate;
    @AuditIgnore
    @Column(name = "LASTMODIFIED_BY", length = 30)
    private String lastModifiedBy;
    @AuditIgnore
    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;
    @AuditIgnore
    @Column(name = "DELETED", length = 1)
    private String deleted = "N";
}
