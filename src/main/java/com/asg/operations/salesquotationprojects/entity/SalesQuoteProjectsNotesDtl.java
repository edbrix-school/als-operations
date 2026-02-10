package com.asg.operations.salesquotationprojects.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.operations.salesquotationprojects.key.SalesQuoteProjectsNotesDtlId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "SALES_QUOTE_PROJECTS_NOTES_DTL"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesQuoteProjectsNotesDtl {

    @EmbeddedId
    private SalesQuoteProjectsNotesDtlId id;

    @Column(name = "NOTES", length = 300)
    private String notes;

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
