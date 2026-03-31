package com.asg.operations.salesquotationprojects.key;

import com.asg.common.lib.annotation.AuditIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GlobalTermsCustomChangesId implements Serializable {

    @AuditIgnore
    @Column(name = "DOC_ID", length = 25, nullable = false)
    private String docId;

    @AuditIgnore
    @Column(name = "DOC_KEY_POID", nullable = false)
    private Long docKeyPoid;

    @AuditIgnore
    @Column(name = "REF_TERMS_POID", nullable = false)
    private Long refTermsPoid;

    @AuditIgnore
    @Column(name = "DET_ROW_ID", nullable = false)
    private Long detRowId;
}
