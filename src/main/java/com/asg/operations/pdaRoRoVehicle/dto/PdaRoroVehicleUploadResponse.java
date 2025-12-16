package com.asg.operations.pdaRoRoVehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdaRoroVehicleUploadResponse {

    private String status;
    private List<PdaRoroVehicleDtlResponseDto> vehicleDetails;
}
