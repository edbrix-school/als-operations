package com.asg.operations.portcalloperation.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "OPS_PC_DOCS_MSGS_DTL1")
public class OpsPcDocsMsgsDtl1 {

    @EmbeddedId
    private OpsPcDocsMsgsDtl1Id id;

    @Column(name = "SEND_BY_POID")
    private Long sendByPoid;

    @Column(name = "EMAIL_SUBJECT", length = 1000)
    private String emailSubject;

    @Column(name = "EMAIL_DOCUMENTS", length = 4000)
    private String emailDocuments;

    @Column(name = "EMAIL_SEND_ON")
    @Temporal(TemporalType.DATE)
    private Date emailSendOn;

    @Column(name = "EMAIL_CONTENT", length = 4000)
    private String emailContent;

    @Column(name = "EMAIL_REMARKS", length = 1000)
    private String emailRemarks;
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
}
