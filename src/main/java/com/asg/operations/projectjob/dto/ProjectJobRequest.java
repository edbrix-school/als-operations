package com.asg.operations.projectjob.dto;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProjectJobRequest extends FFManifestHdrDto {
    @Valid
    private List<ProjectJobAirPkgDtoRequest> airPackages;
    @Valid
    private List<ProjectJobBayanDtoRequest> bayanDetails;
    @Valid
    private List<ProjectJobChargesDtoRequest> charges;
    @Valid
    private List<ProjectJobContainerDtoRequest> containers;
    @Valid
    private List<ProjectJobTruckDtoRequest> truckDetails;

    // Control sheet linking — optional, supplied when creating a job from a control sheet row
    private Long controlSheetTransactionPoid;
    private Long controlSheetDetRowId;
    private String controlSheetDocId;
}