package com.asg.operations.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirControlSheetDTO {
    private Long detRowId;
    private Long jobId;
    private String origin;
    private String destination;
    private LocalDate etd;
    private LocalDate eta;
    private LocalDate arrivalDate;
    private Double noOfPackages;
    private Double weight;
    private Double cbm;
    private String carrier;
    private String description;
    private String jobStatus;
    private String documentStatus;
}
