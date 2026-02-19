package com.asg.operations.projectjob.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectJobAirPkgDtoRequest extends ProjectJobAirPkgDto implements BaseDetailDto {

    private String actionType;
}
