package com.asg.operations.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirPackageDTO {
    private Long detRowId;
    private BigDecimal noOfPacks;
    private String packUnit;
    private BigDecimal totalWeight;
    private BigDecimal totalVolume;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal chargeableWeight;
    private String description;
    private String imcoClassUnno;
    private String properShippingName;
    private String imcoClassDivision;
    private String packingGrouping;
    private String quantityPackingType;
    private String packingInst;
    private String authorisation;
    private LocalDate appointmentDate;
    private LocalDate deliveryDate;
    private String detention;
    private String docStatus;
    private String remarks;
}
