package com.asg.operations.portcalloperation.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "OPS_PC_DOCS_COPY_DTL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallOperationDocsCopyDtlId.class)
public class PortCallOperationDocsCopyDtl extends BaseEntity {

    @AuditIgnore
    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @AuditIgnore
    @Id
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Column(name = "DOCUMENT_FROM", length = 100)
    private String documentFrom;

    @Column(name = "EMAIL_POID")
    private Long emailPoid;

    @Column(name = "DOCUMENT_LIST", length = 4000)
    private String documentList;

    @Column(name = "DOCUMENT_SELECT", length = 1)
    private String documentSelect;

    @Column(name = "DOCUMENT_ATTACHMENTS", length = 4000)
    private String documentAttachments;
}
