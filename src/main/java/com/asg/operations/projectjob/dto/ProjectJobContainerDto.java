package com.asg.operations.projectjob.dto;

import com.asg.common.lib.dto.LovGetListDto;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProjectJobContainerDto {

    private Long detRowId;

    @Size(max = 25, message = "Container number must not exceed 25 characters")
    private String containerNo;

    @Size(max = 1, message = "Equipment shipper own must be exactly 1 character") //Y/N
    private String equipmentShipperOwn;

    @Size(max = 200, message = "Cargo description must not exceed 200 characters")
    private String cargoDescription;

    @Size(max = 25, message = "Container seal number must not exceed 25 characters")
    private String containerSealNo;

    @Size(max = 25, message = "Container ISO code must not exceed 25 characters")
    private String containerIsoCode;

    private BigDecimal containerTypePoid;
    private LovGetListDto containerTypeLov;

    @Size(max = 20, message = "Container size must not exceed 20 characters")
    private String containerSize;
    private BigDecimal quantity;

    private BigDecimal grsVolume;
    private BigDecimal grsWeight;
    private BigDecimal netVolume;
    private BigDecimal netWeight;
    private BigDecimal tareWeight;
    private BigDecimal noOfPacks;

    @Size(max = 20, message = "Pack unit must not exceed 20 characters")
    private String packUnit;

    private BigDecimal comodityPoid;
    private BigDecimal destinationPortPoid;

    @Size(max = 20, message = "IMO must not exceed 20 characters")
    private String imo;

    @Size(max = 20, message = "OOG length must not exceed 20 characters")
    private String oogL;

    private BigDecimal oogB;

    @Size(max = 20, message = "OOG height must not exceed 20 characters")
    private String oogH;

    @Size(max = 20, message = "Refer temperature must not exceed 20 characters")
    private String refferTemp;

    @Size(max = 20, message = "Reefer humidity must not exceed 20 characters")
    private String refferHum;

    @Size(max = 20, message = "Reefer vent must not exceed 20 characters")
    private String refferVent;

    @Size(max = 20, message = "Seal number must not exceed 20 characters")
    private String sealNo;

    private LocalDateTime unloadDate;
    private LocalDateTime cargoCollectionDate;
    private LocalDateTime deliveryDate;

    @Size(max = 100, message = "CFS note must not exceed 100 characters")
    private String cfsNote;

    @Size(max = 100, message = "Damage note must not exceed 100 characters")
    private String damageNote;

    @Size(max = 200, message = "Truck driver details must not exceed 200 characters")
    private String truckDriverDetails;

    @Size(max = 1, message = "Is IMCO must be exactly 1 character")
    private String isImco;

    @Size(max = 100, message = "IMCO class type must not exceed 100 characters")
    private String imcoClassType;

    @Size(max = 100, message = "IMCO class actual must not exceed 100 characters")
    private String imcoClassActual;

    @Size(max = 1, message = "Detention must be exactly 1 character")
    private String detention;

    @Size(max = 100, message = "Document status must not exceed 100 characters")
    private String docStatus;

    @Size(max = 300, message = "Remarks must not exceed 300 characters")
    private String remarks;

}
