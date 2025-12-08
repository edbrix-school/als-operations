package com.asg.operations.shipprincipal.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ShipPrincipalMasterDtlId implements Serializable {
    private Long principalPoid;
    private Long detRowId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShipPrincipalMasterDtlId that = (ShipPrincipalMasterDtlId) o;
        return Objects.equals(principalPoid, that.principalPoid) &&
                Objects.equals(detRowId, that.detRowId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(principalPoid, detRowId);
    }
}
