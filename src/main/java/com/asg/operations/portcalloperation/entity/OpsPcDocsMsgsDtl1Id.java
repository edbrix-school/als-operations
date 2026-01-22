package com.asg.operations.portcalloperation.entity;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpsPcDocsMsgsDtl1Id implements Serializable {

    @Column(name = "TRANSACTION_POID", nullable = false)
    private Long transactionPoid;

    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Column(name = "EMAIL_POID", nullable = false)
    private Long emailPoid;
}
