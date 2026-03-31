package com.asg.operations.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoadFreightDetailedDTO {
    private RoadControlSheetDTO controlSheetInfo;
    private String transportFrom;
    private String transportTo;
    private List<TruckCargoDTO> truckCargoDetails;
    private JobChargesDTO charges;
}
