package com.asg.operations.projectjob.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectJobAirPkgDto {

	private Long detRowId;

    @NotNull(message = "Number of packs is required")
	private Long noOfPacks;
    @NotNull(message = "Pack unit is required")
	private String packUnit;

    @NotNull(message = "total weight is required")
	private BigDecimal totalWeight;
	private BigDecimal totalVolume;

    @NotNull(message = "Length is required")
	private BigDecimal length;
    @NotNull(message = "Width is required")
	private BigDecimal width;
    @NotNull(message = "Height is required")
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
