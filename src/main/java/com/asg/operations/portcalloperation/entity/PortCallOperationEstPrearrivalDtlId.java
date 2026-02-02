package com.asg.operations.portcalloperation.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PortCallOperationEstPrearrivalDtlId implements Serializable {
    private Long transactionPoid;
    private Long detRowId;
}
