package com.asg.operations.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FreightFilterRequest {
    private String freightType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String jobStatus;
    private String searchTerm;
    private Long linePoid;
    private Long carrierPoid;
    private String pol;
    private String pod;
    private Boolean upcomingOnly;
}
