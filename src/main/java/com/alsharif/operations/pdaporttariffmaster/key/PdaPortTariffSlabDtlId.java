package com.alsharif.operations.pdaporttariffmaster.key;

import jakarta.persistence.*;
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

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "slabDtlSeq")
    @SequenceGenerator(name = "slabDtlSeq",
            sequenceName = "PDA_PORT_TARIFF_SLAB_DTL_SEQ",
            allocationSize = 1)
    @Column(name = "DET_ROW_ID", nullable = false)
    private Long detRowId;

    @Column(name = "CHARGE_DET_ROW_ID", nullable = false)
    private Long chargeDetRowId;
}
