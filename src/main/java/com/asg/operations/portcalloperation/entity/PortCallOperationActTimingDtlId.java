package com.asg.operations.portcalloperation.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PortCallOperationActTimingDtlId implements Serializable {
    private Long transactionPoid;
    private Long detRowId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PortCallOperationActTimingDtlId)) return false;
        PortCallOperationActTimingDtlId that = (PortCallOperationActTimingDtlId) o;
        return Objects.equals(transactionPoid, that.transactionPoid)
                && Objects.equals(detRowId, that.detRowId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionPoid, detRowId);
    }
}
