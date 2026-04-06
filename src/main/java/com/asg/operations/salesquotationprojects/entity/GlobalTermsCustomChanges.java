package com.asg.operations.salesquotationprojects.entity;

import com.asg.common.lib.entity.BaseEntity;
import com.asg.operations.salesquotationprojects.key.GlobalTermsCustomChangesId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "GLOBAL_TERMS_CUSTOM_CHANGES")
@NoArgsConstructor
@AllArgsConstructor
public class GlobalTermsCustomChanges extends BaseEntity {

    @EmbeddedId
    private GlobalTermsCustomChangesId id;

    @Column(name = "COMPANY_POID", nullable = false)
    private Long companyPoid;

    @Column(name = "CLAUSE_NO", length = 100)
    private String clauseNo;

    @Column(name = "CLAUSE_DETAILS", length = 2000)
    private String clauseDetails;

    @Column(name = "ACTIVE", length = 1)
    private String active;

}
