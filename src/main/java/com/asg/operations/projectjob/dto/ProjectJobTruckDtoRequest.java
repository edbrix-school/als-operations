package com.asg.operations.projectjob.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectJobTruckDtoRequest extends ProjectJobTruckDto implements BaseDetailDto {

    private String actionType;
}
