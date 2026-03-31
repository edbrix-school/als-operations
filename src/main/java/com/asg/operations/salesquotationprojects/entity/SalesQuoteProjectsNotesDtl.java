package com.asg.operations.salesquotationprojects.entity;

import com.asg.common.lib.entity.BaseEntity;
import com.asg.operations.salesquotationprojects.key.SalesQuoteProjectsNotesDtlId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "SALES_QUOTE_PROJECTS_NOTES_DTL"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesQuoteProjectsNotesDtl extends BaseEntity {

    @EmbeddedId
    private SalesQuoteProjectsNotesDtlId id;

    @Column(name = "NOTES", length = 300)
    private String notes;
}
