package com.alsharif.operations.shipprincipal.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AddressDetailsId implements Serializable {
    private Long addressMasterPoid;
    private Long addressPoid;
}
