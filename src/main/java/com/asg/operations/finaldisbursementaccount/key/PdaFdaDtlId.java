package com.asg.operations.finaldisbursementaccount.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PdaFdaDtlId implements Serializable {
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;
    @Column(name = "DET_ROW_ID")
    private Long detRowId;
}
