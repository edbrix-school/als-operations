package com.asg.operations.projectjob.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectJobBayanDtoRequest extends ProjectJobBayanDto implements BaseDetailDto {

    private String actionType;
}
