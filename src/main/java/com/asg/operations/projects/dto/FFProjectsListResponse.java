package com.asg.operations.projects.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FFProjectsListResponse {

    private Long transactionPoid;
    private LocalDate transactionDate;
    private String docRef;
    private String projectDescription;
    private String projectStatus;
    private LocalDate periodFrom;
    private LocalDate periodTo;
    private Double grandTotalSellRateBhd;
}
