package com.asg.operations.projectjob.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectJobContainerDtoRequest extends ProjectJobContainerDto implements BaseDetailDto {

    private String actionType;
}
