package com.asg.operations.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeaControlSheetDTO {
    private Long detRowId;
    private Long jobId;
    private String pol;
    private String pod;
    private LocalDate etd;
    private LocalDate eta;
    private LocalDate arrivalDate;
    private LocalDate sailDate;
    private Double weight;
    private Double cbm;
    private String line;
    private String vesselName;
    private String description;
    private String jobStatus;
    private String documentStatus;
}
