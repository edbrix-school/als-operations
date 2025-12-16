package com.asg.operations.pdaRoRoVehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdaRoroVehicleUploadRequest {
    private Long transactionPoid;
    private Long voyagePoid;
    private LocalDate docDate;
}
