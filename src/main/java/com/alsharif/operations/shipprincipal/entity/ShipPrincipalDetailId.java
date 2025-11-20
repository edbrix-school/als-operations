package com.alsharif.operations.shipprincipal.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ShipPrincipalDetailId implements Serializable {
    private Long principalPoid;
    private Long detRowId;
}
