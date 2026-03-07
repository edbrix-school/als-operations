package com.asg.operations.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingJobDTO {
    private Long detRowId;
    private Long jobId;
    private String jobNo;
    private String freightType;
    private String line;
    private LocalDate eta;
    private LocalDate etd;
    private String pol;
    private String pod;
    private String origin;
    private String destination;
    private String description;
    private Double cbm;
    private Double packages;
    private Double weight;
    private String jobStatus;
    private Boolean canCreateJob;
}
