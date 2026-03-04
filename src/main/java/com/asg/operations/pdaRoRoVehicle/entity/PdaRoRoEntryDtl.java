package com.asg.operations.pdaRoRoVehicle.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PDA_RORO_ENTRY_DTL")
public class PdaRoRoEntryDtl extends BaseEntity {

    @EmbeddedId
    private PdaRoRoEntryDtlId id;

    @Column(name = "BL_NUMBER", length = 300)
    private String blNumber;

    @Column(name = "SHIPPER", length = 500)
    private String shipper;

    @Column(name = "CONSIGNEE", length = 500)
    private String consignee;

    @Column(name = "VIN_NUMBER", length = 500)
    private String vinNumber;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "BL_GWT")
    private Double blGwt;

    @Column(name = "BL_CBM")
    private Double blCbm;

    @AuditIgnore
    @Column(name = "PORT_OF_LOAD", length = 500)
    private String portOfLoad;

    @AuditIgnore
    @Column(name = "AGENT", length = 500)
    private String agent;

    @Column(name = "REMARKS", length = 2000)
    private String remarks;
}
