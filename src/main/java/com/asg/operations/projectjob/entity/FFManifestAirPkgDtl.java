package com.asg.operations.projectjob.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "FF_MANIFEST_AIR_PKG_DETAILS")
@IdClass(FFManifestAirPkgDtlId.class)
public class FFManifestAirPkgDtl extends BaseEntity implements BaseDetailEntity{
    @AuditIgnore
    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    @NotNull
    private Long transactionPoid;

    @AuditIgnore
    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    @NotNull
    private Long detRowId;

    @Column(name = "NO_OF_PACKS", nullable = false)
    @NotNull
    private Long noOfPacks;

    @Column(name = "PACK_UNIT", nullable = false)
    @NotNull
    @Size(max = 20)
    private String packUnit;

    @Column(name = "TOTAL_WEIGHT", nullable = false)
    @NotNull
    private BigDecimal totalWeight;

    @Column(name = "TOTAL_VOLUME")
    private BigDecimal totalVolume;

    @Column(name = "LENGTH", nullable = false)
    @NotNull
    private BigDecimal length;

    @Column(name = "WIDTH", nullable = false)
    @NotNull
    private BigDecimal width;

    @Column(name = "HEIGHT", nullable = false)
    @NotNull
    private Long height;

    @Column(name = "IMCO_CLASS_UNNO")
    @Size(max = 25)
    private String imcoClassUnno;

    @Column(name = "PROPER_SHIPPING_NAME")
    @Size(max = 50)
    private String properShippingName;

    @Column(name = "IMCO_CLASS_DIVISION")
    @Size(max = 25)
    private String imcoClassDivision;

    @Column(name = "PACKING_GROUPING")
    @Size(max = 25)
    private String packingGrouping;

    @Column(name = "QUANTITY_PACKING_TYPE")
    @Size(max = 100)
    private String quantityPackingType;

    @Column(name = "PACKING_INST")
    @Size(max = 25)
    private String packingInst;

    @Column(name = "AUTHORISATION")
    @Size(max = 25)
    private String authorisation;

    @Column(name = "DESCRIPTION")
    @Size(max = 200)
    private String description;

    @Column(name = "APPOINTMENT_DATE")
    private LocalDateTime appointmentDate;

    @Column(name = "DELIVERY_DATE")
    private LocalDateTime deliveryDate;

    @Column(name = "DETENTION")
    @Size(max = 1)
    private String detention;

    @Column(name = "DOC_STATUS")
    @Size(max = 100)
    private String docStatus;

    @Column(name = "REMARKS")
    @Size(max = 300)
    private String remarks;

    @Column(name = "CHARGEABLE_WEIGHT")
    private BigDecimal chargeableWeight;

}
