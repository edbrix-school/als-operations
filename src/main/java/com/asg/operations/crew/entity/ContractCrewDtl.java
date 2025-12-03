package com.asg.operations.crew.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity class for CONTRACT_CREW_DTL detail table
 * Represents visa/document details for a crew member
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "CONTRACT_CREW_DTL")
public class ContractCrewDtl {

//    @Id
//    @Column(name = "CREW_POID", nullable = false)
//    @NotNull
//    private Long crewPoid;
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "crew_dtl_row_id_seq")
//    @SequenceGenerator(name = "crew_dtl_row_id_seq", sequenceName = "CREW_DTL_ROW_ID_SEQ", allocationSize = 1)
//    @Column(name = "DET_ROW_ID", nullable = false)
//    private Long detRowId;

    @EmbeddedId
    private ContractCrewDtlId id;

    @Column(name = "DOCUMENT_TYPE", length = 50, nullable = false)
    @NotBlank
    @Size(max = 50)
    private String documentType;

    @Column(name = "DOCUMENT_NUMBER", length = 100, nullable = false)
    @NotBlank
    @Size(max = 100)
    private String documentNumber;

    @Column(name = "DOCUMENT_APPLIED_DATE", nullable = false)
    @NotNull
    private LocalDate documentAppliedDate;

    @Column(name = "DOCUMENT_ISSUE_DATE")
    private LocalDate documentIssueDate;

    @Column(name = "DOCUMENT_EXPIRY_DATE")
    private LocalDate documentExpiryDate;

    @Column(name = "PPT_RECEIPT_DATE")
    private LocalDate pptReceiptDate;

    @Column(name = "PPT_RETURN_DATE")
    private LocalDate pptReturnDate;

    @Column(name = "REMARKS", length = 500)
    @Size(max = 500)
    private String remarks;

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


    @Column(name = "ACTIVE", length = 1)
    @Size(max = 1)
    private String active;

}

