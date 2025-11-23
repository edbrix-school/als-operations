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
public class PdaPortTariffChargeDtlId implements Serializable {
    @Column(name = "TRANSACTION_POID", nullable = false)
    private Long transactionPoid;

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chargeDtlSeq")
    @SequenceGenerator(name = "chargeDtlSeq",
            sequenceName = "PDA_PORT_TARIFF_CHARGE_DTL_SEQ",
            allocationSize = 1)
    @Column(name = "DET_ROW_ID", nullable = false)
    private Long detRowId;
}

