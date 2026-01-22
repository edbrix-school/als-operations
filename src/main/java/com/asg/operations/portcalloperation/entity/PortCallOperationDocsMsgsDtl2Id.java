package com.asg.operations.portcalloperation.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortCallOperationDocsMsgsDtl2Id implements Serializable {
    private Long transactionPoid;
    private Long detRowId;
}
