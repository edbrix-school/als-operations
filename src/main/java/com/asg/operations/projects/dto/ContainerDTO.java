package com.asg.operations.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContainerDTO {
    private Long detRowId;
    private String containerNo;
    private String containerSealNo;
    private String containerIsoCode;
    private Long containerTypePoid;
    private String containerSize;
    private BigDecimal quantity;
    private BigDecimal grossVolume;
    private BigDecimal grossWeight;
    private BigDecimal netVolume;
    private BigDecimal netWeight;
    private BigDecimal tareWeight;
    private BigDecimal noOfPacks;
    private String packUnit;
    private String imo;
    private String imcoClassType;
    private String imcoClassActual;
    private String isImco;
    private BigDecimal oogL;
    private BigDecimal oogB;
    private BigDecimal oogH;
    private BigDecimal refferTemp;
    private BigDecimal refferHum;
    private BigDecimal refferVent;
    private String sealNo;
    private LocalDate deliveryDate;
    private String detention;
    private String docStatus;
    private String remarks;
    private LocalDate unloadDate;
    private String cfsNote;
    private String damageNote;
    private LocalDate cargoCollectionDate;
    private String truckDriverDetails;
}
