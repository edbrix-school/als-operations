package com.asg.operations.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeaFreightSummaryDTO {
    private Long detRowId;
    private Long jobId;
    private String jobNo;
    private String vesselName;
    private String voyageNo;
    private String motherVesselName;
    private String motherVoyageNo;
    private String pol;
    private String pod;
    private String masterBlNo;
    private String houseBlNo;
    private String houseBlNo2;
    private LocalDate eta;
    private LocalDate etd;
    private LocalDate arrivalDate;
    private LocalDate sailDate;
    private String bookingRef;
    private String releaseType;
    private Double weight;
    private Double cbm;
    private String line;
    private String description;
    private String jobStatus;
    private String documentStatus;
    private String radioActive;
    private String ofoqManifestRef;
    private String remarks;
    // Container fields
    private String containerNo;
    private String containerType;
    private String sealNumber;
    private String cargoDescription;
    private Double qty;
    private Double qtyPackages;
    private LocalDate appointmentDate;
    private LocalDate deliveryDate;
    private String detention;
    private String destuffingFull;
    private String docStatus;
}
