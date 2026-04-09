package com.asg.operations.projectjob.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProjectJobContainerDto {

    private Long detRowId;

    private String containerNo;
    private String equipmentShipperOwn; // Y / N
    private String cargoDescription;
    private String containerSealNo;
    private String containerIsoCode;
    private Long containerTypePoid;

    private String containerSize;
    private BigDecimal quantity;

    private BigDecimal grsVolume;
    private BigDecimal grsWeight;
    private BigDecimal netVolume;
    private BigDecimal netWeight;
    private BigDecimal tareWeight;

    private BigDecimal noOfPacks;
    private String packUnit;

    private Long comodityPoid;
    private Long destinationPortPoid;

    private String imo;

    private String oogL;
    private BigDecimal oogB;
    private String oogH;

    private String refferTemp;
    private String refferHum;
    private String refferVent;

    private String sealNo;

    private LocalDateTime unloadDate;
    private LocalDateTime cargoCollectionDate;
    private LocalDateTime deliveryDate;

    private String cfsNote;
    private String damageNote;
    private String truckDriverDetails;

    private String isImco;
    private String imcoClassType;
    private String imcoClassActual;

    private String detention;
    private String docStatus;
    private String remarks;

}
