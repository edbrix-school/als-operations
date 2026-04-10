package com.asg.operations.projectjob.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProjectJobAirPkgDto {

    private Long detRowId;

    private Long noOfPacks;
    private String packUnit;

    private BigDecimal totalWeight;
    private BigDecimal totalVolume;

    private BigDecimal length;
    private BigDecimal width;
    private Long height;

    private String imcoClassUnno;
    private String properShippingName;
    private String imcoClassDivision;
    private String packingGrouping;
    private String quantityPackingType;
    private String packingInst;
    private String authorisation;
    private String description;

    private LocalDateTime appointmentDate;
    private LocalDateTime deliveryDate;

    private String detention;
    private String docStatus;
    private String remarks;

    private BigDecimal chargeableWeight;

}
