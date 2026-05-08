package com.asg.operations.projectjob.dto;

import com.asg.common.lib.dto.LovGetListDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProjectJobResponse extends FFManifestHdrDtoResponse {
    private List<ProjectJobAirPkgDto> airPackages;
    private List<ProjectJobBayanDto> bayanDetails;
    private List<ProjectJobChargesDto> charges;
    private List<ProjectJobContainerDto> containers;
    private List<ProjectJobTruckDto> truckDetails;
    private LovGetListDto projectCustomerPoidLov;
}