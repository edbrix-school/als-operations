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
public class ProjectLoadInJobsProcResponse {

    private String status;
    private List<ProjectLoadHdrRow> header;
    private List<ProjectLoadDtlRow> details;
}
