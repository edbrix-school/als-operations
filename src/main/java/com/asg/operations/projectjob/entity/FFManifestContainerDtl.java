package com.asg.operations.projectjob.entity;

import com.asg.common.lib.annotation.AuditIgnore;
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
@Table(name = "FF_MANIEST_CONTAINER_DTL")
@IdClass(FFManifestContainerDtlId.class)
public class FFManifestContainerDtl implements BaseDetailEntity {

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

    @Column(name = "CONTAINER_NO")
    @Size(max = 25)
    private String containerNo;

    @Column(name = "EQUIPMENT_SHIPPER_OWN")
    @Size(max = 1)
    private String equipmentShipperOwn; // "Y" or "N"

    @Column(name = "CARGO_DESCRIPTION")
    @Size(max = 200)
    private String cargoDescription;

    @Column(name = "CONTAINER_SEAL_NO")
    @Size(max = 25)
    private String containerSealNo;

    @Column(name = "CONTAINER_ISOCODE")
    @Size(max = 25)
    private String containerIsoCode;

    @Column(name = "CONTAINER_TYPE_POID")
    private BigDecimal conatinerTypePoid;

    @Column(name = "CONTAINER_SIZE")
    @Size(max = 20)
    private String containerSize;

    @Column(name = "QUANTITY")
    private BigDecimal quantity;

    @Column(name = "GRS_VOLUME")
    private BigDecimal grsVolume;

    @Column(name = "GRS_WEIGHT")
    private BigDecimal grsWeight;

    @Column(name = "NET_VOLUME")
    private BigDecimal netVolume;

    @Column(name = "NET_WEIGHT")
    private BigDecimal netWeight;

    @Column(name = "TARE_WEIGHT")
    private BigDecimal tareWeight;

    @Column(name = "NO_OF_PACKS")
    private BigDecimal noOfPacks;

    @Column(name = "PACK_UNIT")
    @Size(max = 20)
    private String packUnit;

    @Column(name = "COMODITY_POID")
    private BigDecimal comodityPoid;

    @Column(name = "DESTINATION_PORT_POID")
    private BigDecimal destinationPortPoid;

    @Column(name = "IMO")
    @Size(max = 20)
    private String imo;

    @Column(name = "OOG_L")
    @Size(max = 20)
    private String oogL;

    @Column(name = "OOG_B")
    private BigDecimal oogB;

    @Column(name = "OOG_H")
    @Size(max = 20)
    private String oogH;

    @Column(name = "REFFER_TEMP")
    @Size(max = 20)
    private String refferTemp;

    @Column(name = "REFFER_HUM")
    @Size(max = 20)
    private String refferHum;

    @Column(name = "REFFER_VENT")
    @Size(max = 20)
    private String refferVent;

    @AuditIgnore
    @Column(name = "CREATED_BY")
    @Size(max = 20)
    private String createdBy;

    @AuditIgnore
    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @AuditIgnore
    @Column(name = "LASTMODIFIED_BY")
    @Size(max = 20)
    private String lastModifiedBy;

    @AuditIgnore
    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @Column(name = "SEAL_NO")
    @Size(max = 20)
    private String sealNo;

    @Column(name = "UNLOAD_DATE")
    private LocalDateTime unloadDate;

    @Column(name = "CFS_NOTE")
    @Size(max = 100)
    private String cfsNote;

    @Column(name = "DAMAGE_NOTE")
    @Size(max = 100)
    private String damageNote;

    @Column(name = "CARGO_COLLECTION_DATE")
    private LocalDateTime cargoCollectionDate;

    @Column(name = "TRUCK_DRIVER_DETAILS")
    @Size(max = 200)
    private String truckDriverDetails;

    @Column(name = "IS_IMCO")
    @Size(max = 1)
    private String isImco;

    @Column(name = "IMCO_CLASS_TYPE")
    @Size(max = 100)
    private String imcoClassType;

    @Column(name = "IMCO_CLASS_ACTUAL")
    @Size(max = 100)
    private String ImcoClassActual;

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
}
