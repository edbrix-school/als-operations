package com.asg.operations.pdaRoRoVehicle.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdaRoRoVehicleUploadRequest {

    @NotNull(message = "Transaction POID is required")
    private Long transactionPoid;
    
    @NotNull(message = "Voyage POID is required")
    private Long voyagePoid;
    
    @NotNull(message = "Document date is required")
    private LocalDate docDate;
}
