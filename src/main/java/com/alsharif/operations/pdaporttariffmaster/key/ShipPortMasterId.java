package com.alsharif.operations.pdaporttariffmaster.key;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipPortMasterId implements Serializable {

    @Column(name = "PORT_POID")
    private BigDecimal portPoid;

    @Column(name = "GROUP_POID")
    private BigDecimal groupPoid;
}
