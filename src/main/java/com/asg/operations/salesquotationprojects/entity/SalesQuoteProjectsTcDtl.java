package com.asg.operations.salesquotationprojects.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.operations.salesquotationprojects.key.SalesQuoteProjectsTcDtlId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "SALES_QUOTE_PROJECTS_TC_DTL"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesQuoteProjectsTcDtl {

    @EmbeddedId
    private SalesQuoteProjectsTcDtlId id;

    @Column(name = "CLAUSE_REF", length = 50)
    private String clauseRef;

    @Column(name = "TERMS_DESCRIPTION", length = 500)
    private String termsDescription;

    @AuditIgnore
    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @AuditIgnore
    @Column(name = "CREATED_DATE")
    private LocalDate createdDate;

    @AuditIgnore
    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @AuditIgnore
    @Column(name = "LASTMODIFIED_DATE")
    private LocalDate lastModifiedDate;
}
