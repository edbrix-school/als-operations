package com.alsharif.operations.pdaentryform.entity;

import java.io.Serializable;
import java.util.Objects;

public class PdaEntryAcknowledgmentDtlId implements Serializable {

    private Long transactionPoid;
    private Long detRowId;

    public PdaEntryAcknowledgmentDtlId() {
    }

    public PdaEntryAcknowledgmentDtlId(Long transactionPoid, Long detRowId) {
        this.transactionPoid = transactionPoid;
        this.detRowId = detRowId;
    }

    public Long getTransactionPoid() {
        return transactionPoid;
    }

    public void setTransactionPoid(Long transactionPoid) {
        this.transactionPoid = transactionPoid;
    }

    public Long getDetRowId() {
        return detRowId;
    }

    public void setDetRowId(Long detRowId) {
        this.detRowId = detRowId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PdaEntryAcknowledgmentDtlId that = (PdaEntryAcknowledgmentDtlId) o;
        return Objects.equals(transactionPoid, that.transactionPoid) &&
                Objects.equals(detRowId, that.detRowId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionPoid, detRowId);
    }
}

