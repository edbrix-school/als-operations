package com.asg.operations.pdaporttariffmaster.key;

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
public class PdaPortTariffSlabDtlId implements Serializable {
    @Column(name = "TRANSACTION_POID", nullable = false)
    private Long transactionPoid;

    @Column(name = "DET_ROW_ID", nullable = false)
    private Long detRowId;

    @Column(name = "CHARGE_DET_ROW_ID", nullable = false)
    private Long chargeDetRowId;
}
