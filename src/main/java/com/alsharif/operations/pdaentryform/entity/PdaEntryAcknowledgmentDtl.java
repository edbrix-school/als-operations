package com.alsharif.operations.pdaentryform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "PDA_ENTRY_ACKNOWLEDGMENT_DTL")
@IdClass(PdaEntryAcknowledgmentDtlId.class)
public class PdaEntryAcknowledgmentDtl {

    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    @NotNull
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    @NotNull
    private Long detRowId;

    @Column(name = "PARTICULARS", length = 2000)
    @Size(max = 2000)
    private String particulars;

    @Column(name = "SELECTED", length = 1)
    @Size(max = 1)
    private String selected;

    @Column(name = "REMARKS", length = 4000)
    @Size(max = 4000)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSACTION_POID", insertable = false, updatable = false)
    private PdaEntryHdr entryHdr;

    // Getters and Setters

    public void setTransactionPoid(Long transactionPoid) {
        this.transactionPoid = transactionPoid;
    }

    public void setDetRowId(Long detRowId) {
        this.detRowId = detRowId;
    }

    public void setParticulars(String particulars) {
        this.particulars = particulars;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public void setEntryHdr(PdaEntryHdr entryHdr) {
        this.entryHdr = entryHdr;
    }
}

