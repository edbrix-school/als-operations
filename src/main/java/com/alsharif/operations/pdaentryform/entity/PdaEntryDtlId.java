package com.alsharif.operations.pdaentryform.entity;

import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Getter
public class PdaEntryDtlId implements Serializable {

    private Long transactionPoid;
    private Long detRowId;

    public PdaEntryDtlId() {
    }

    public PdaEntryDtlId(Long transactionPoid, Long detRowId) {
        this.transactionPoid = transactionPoid;
        this.detRowId = detRowId;
    }

    public void setTransactionPoid(Long transactionPoid) {
        this.transactionPoid = transactionPoid;
    }

    public void setDetRowId(Long detRowId) {
        this.detRowId = detRowId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PdaEntryDtlId that = (PdaEntryDtlId) o;
        return Objects.equals(transactionPoid, that.transactionPoid) &&
                Objects.equals(detRowId, that.detRowId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionPoid, detRowId);
    }
}

