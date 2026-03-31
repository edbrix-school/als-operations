package com.asg.operations.salesquotationprojects.entity;

import com.asg.common.lib.entity.BaseEntity;
import com.asg.operations.salesquotationprojects.key.SalesQuoteProjectsTcDtlId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "SALES_QUOTE_PROJECTS_TC_DTL"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesQuoteProjectsTcDtl extends BaseEntity {

    @EmbeddedId
    private SalesQuoteProjectsTcDtlId id;

    @Column(name = "CLAUSE_REF", length = 50)
    private String clauseRef;

    @Column(name = "TERMS_DESCRIPTION", length = 500)
    private String termsDescription;
}
