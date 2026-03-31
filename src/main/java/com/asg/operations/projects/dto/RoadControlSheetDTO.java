package com.asg.operations.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoadControlSheetDTO {
    private Long detRowId;
    private Long jobId;
    private String truckNumber;
    private String transportFrom;
    private String transportTo;
    private LocalDate eta;
    private Double weight;
    private Double cbm;
    private String description;
    private String jobStatus;
    private String documentStatus;
}
