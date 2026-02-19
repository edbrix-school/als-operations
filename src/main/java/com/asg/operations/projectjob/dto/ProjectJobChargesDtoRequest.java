package com.asg.operations.projectjob.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectJobChargesDtoRequest extends ProjectJobChargesDto implements BaseDetailDto {

    private String actionType;
}
