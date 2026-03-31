package com.asg.operations.projects.dto;

import com.asg.common.lib.dto.LovGetListDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FreightSummaryDTO {
    private Long detRowId;
    private Long jobId;
    private String jobNo;
    private Long principalPoid;
    private LovGetListDto principalLov;
    private String freightType;
    private String description;
    private Double weight;
    private Double cbm;
    private LocalDate eta;
    private LocalDate etd;
    private LocalDate arrivalDate;
    private String jobStatus;
    private String documentStatus;
    private String origin;
    private LovGetListDto originLov;
    private String destination;
    private LovGetListDto destinationLov;
    private String carrier;
    private LovGetListDto carrierLov;
    private Double packages;
    private String pol;
    private LovGetListDto polLov;
    private String pod;
    private LovGetListDto podLov;
    private String line;
    private LovGetListDto lineLov;
    private LocalDate sailDate;
    private String vesselName;
    private String truckNumber;
    private String transportFrom;
    private String transportTo;
    private String blAwbNo;
}
