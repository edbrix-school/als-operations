package com.asg.operations.salesquotationprojects.key;

import com.asg.common.lib.annotation.AuditIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ShipCommodityMasterId implements Serializable {


    @Column(name = "COMODITY_POID")
    private Long commodityPoid;

    @AuditIgnore
    @Column(name = "GROUP_POID")
    private Long groupPoid;
}
