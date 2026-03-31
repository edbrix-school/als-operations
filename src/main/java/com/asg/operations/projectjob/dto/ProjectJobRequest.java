package com.asg.operations.projectjob.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProjectJobRequest extends FFManifestHdrDto {
    private List<ProjectJobAirPkgDtoRequest> airPackages;
    private List<ProjectJobBayanDtoRequest> bayanDetails;
    private List<ProjectJobChargesDtoRequest> charges;
    private List<ProjectJobContainerDtoRequest> containers;
    private List<ProjectJobTruckDtoRequest> truckDetails;

    // Control sheet linking — optional, supplied when creating a job from a control sheet row
    private Long controlSheetTransactionPoid;
    private Long controlSheetDetRowId;
    private String controlSheetDocId;
}