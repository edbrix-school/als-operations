package com.asg.operations.shipprincipal.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipPrincipalPaRptDtlId implements Serializable {
    private Long principalPoid;
    private Long detRowId;
}
