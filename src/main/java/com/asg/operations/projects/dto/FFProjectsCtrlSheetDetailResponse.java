package com.asg.operations.projects.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FFProjectsCtrlSheetDetailResponse {

    private Long transactionPoid;
    private Long detRowId;
    private String freightType;
    private Long jobNoPoid;
    private Long originPoid;
    private Long destinationPoid;
    private LocalDate etd;
    private LocalDate etaAta;
    private LocalDate arrivalDate;
    private Double noOfPackages;
    private Double weight;
    private Double cbm;
    private Long carrierPoid;
    private Long linePoid;
    private String truckNumber;
    private String description;
    private LocalDate sailDate;
    private String jobStatus;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
}
