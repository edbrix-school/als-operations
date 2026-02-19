package com.asg.operations.projectjob.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectJobsSpResult {

    private List<ProjectJobHeaderDto> headerList;
    private List<ProjectJobChargesDto> chargesList;
    private String status;
}
