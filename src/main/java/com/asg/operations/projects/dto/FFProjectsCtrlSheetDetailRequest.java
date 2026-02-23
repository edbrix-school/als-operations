package com.asg.operations.projects.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FFProjectsCtrlSheetDetailRequest {

    private String actionType; // CREATE, UPDATE, DELETE
    private Long detRowId;
    private String freightType; // AIR, SEA, ROAD
    private Long jobNoPoid;
    private Long originPoid;
    private Long destinationPoid;
    private LocalDate etd;
    private LocalDate etaAta;
    private LocalDate arrivalDate;
    private Double noOfPackages;
    private Double weight;
    private Double cbm;
    private Long carrierPoid; // For Air Freight
    private Long linePoid; // For Sea Freight
    private String truckNumber; // For Road Freight
    private String description;
    private LocalDate sailDate; // For Sea Freight
}
