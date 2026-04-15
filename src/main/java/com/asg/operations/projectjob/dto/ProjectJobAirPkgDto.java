package com.asg.operations.projectjob.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProjectJobAirPkgDto {


    private Long detRowId;

    private Long noOfPacks;

    @Size(max = 20, message = "Pack Unit must not exceed 20 characters")
    private String packUnit;

    private BigDecimal totalWeight;
    private BigDecimal totalVolume;

    private BigDecimal length;
    private BigDecimal width;
    private Long height;

    @Size(max = 25, message = "IMCO Class/UN No must not exceed 25 characters")
    private String imcoClassUnno;

    @Size(max = 50, message = "Proper shipping name must not exceed 50 characters")
    private String properShippingName;

    @Size(max = 25, message = "IMCO class division must not exceed 25 characters")
    private String imcoClassDivision;

    @Size(max = 25, message = "Packing grouping must not exceed 25 characters")
    private String packingGrouping;

    @Size(max = 100, message = "Quantity packing type must not exceed 100 characters")
    private String quantityPackingType;

    @Size(max = 25, message = "Packing instruction must not exceed 25 characters")
    private String packingInst;

    @Size(max = 25, message = "Authorisation must not exceed 25 characters")
    private String authorisation;

    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;

    private LocalDateTime appointmentDate;
    private LocalDateTime deliveryDate;

    @Size(max = 1, message = "Detention must be exactly 1 character")
    private String detention;

    @Size(max = 100, message = "Document status must not exceed 100 characters")
    private String docStatus;

    @Size(max = 300, message = "Remarks must not exceed 300 characters")
    private String remarks;

    private BigDecimal chargeableWeight;

}
