package com.asg.operations.projects.dto;

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
    private String destination;
    private String carrier;
    private Double packages;
    private String pol;
    private String pod;
    private String line;
    private LocalDate sailDate;
    private String vesselName;
    private String truckNumber;
    private String transportFrom;
    private String transportTo;
}
