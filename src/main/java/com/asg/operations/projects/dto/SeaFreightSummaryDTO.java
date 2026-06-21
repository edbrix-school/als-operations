package com.asg.operations.projects.dto;

import com.asg.common.lib.dto.LovGetListDto;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    private LovGetListDto polLov;
    private String pod;
    private LovGetListDto podLov;
    private String masterBlNo;
    private String houseBlNo;
    private String houseBlNo2;
    @JsonFormat(pattern = "dd-MMM-yy")
    private LocalDate eta;
    @JsonFormat(pattern = "dd-MMM-yy")
    private LocalDate etd;
    @JsonFormat(pattern = "dd-MMM-yy")
    private LocalDate arrivalDate;
    @JsonFormat(pattern = "dd-MMM-yy")
    private LocalDate sailDate;
    private String bookingRef;
    private String releaseType;
    private LovGetListDto releaseLov;
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
    private LovGetListDto containerTypeLov;
    private String sealNumber;
    private String cargoDescription;
    private Double qty;
    private Double qtyPackages;
    @JsonFormat(pattern = "dd-MMM-yy")
    private LocalDate appointmentDate;
    @JsonFormat(pattern = "dd-MMM-yy")
    private LocalDate deliveryDate;
    private String detention;
    private String destuffingFull;
    private String docStatus;
}
