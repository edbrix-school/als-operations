package com.asg.operations.salesquotationprojects.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "GLOBAL_TAX_MASTER")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalTaxMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_tax_master_seq")
    @SequenceGenerator(
            name = "global_tax_master_seq",
            sequenceName = "GLOBAL_TAX_MASTER_SEQ",
            allocationSize = 1
    )
    @Column(name = "TAX_POID", nullable = false)
    private Long taxPoid;

    @AuditIgnore
    @Column(name = "GROUP_POID", nullable = false)
    private Long groupPoid;

    @Column(name = "TAX_CODE", length = 20, nullable = false)
    private String taxCode;

    @Column(name = "TAX_TYPE", length = 20, nullable = false)
    private String taxType;

    @Column(name = "TAX_NAME", length = 200, nullable = false)
    private String taxName;

    @Column(name = "TAX_NAME2", length = 200)
    private String taxName2;

    @Column(name = "ACTIVE", length = 1)
    private String active = "Y";

    @Column(name = "SEQNO", precision = 5)
    private Integer seqNo;

    @AuditIgnore
    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @AuditIgnore
    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @AuditIgnore
    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @AuditIgnore
    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @AuditIgnore
    @Column(name = "DELETED", length = 1)
    private String deleted = "N";

    @Column(name = "PERCENTAGE")
    private Double percentage;

    @Column(name = "TAX_INPUT_OUTPUT", length = 50)
    private String taxInputOutput;

    @Column(name = "GL_CREDIT_DEBIT", length = 50)
    private String glCreditDebit;

    @Column(name = "GL_POID")
    private Long glPoid;

    @Column(name = "TAX_CATEGORY", length = 25)
    private String taxCategory;

    @Column(name = "NBR_TAX_CODE", length = 100)
    private String nbrTaxCode;

    @Column(name = "NBR_TAX_NAME", length = 500)
    private String nbrTaxName;
}
