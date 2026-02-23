package com.asg.operations.projectjob.entity;

import java.io.Serializable;
import java.util.Objects;

public class FFManifestBayanDtlId implements Serializable {
    private Long transactionPoid;
    private Long detRowId;

    public FFManifestBayanDtlId() {
    }

    public FFManifestBayanDtlId(Long transactionPoid, Long detRowId) {
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
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FFManifestBayanDtlId that = (FFManifestBayanDtlId) o;
        return Objects.equals(transactionPoid, that.transactionPoid) && Objects.equals(detRowId, that.detRowId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionPoid, detRowId);
    }
}
