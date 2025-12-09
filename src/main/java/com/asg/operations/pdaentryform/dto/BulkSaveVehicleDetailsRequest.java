package com.asg.operations.pdaentryform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for bulk save of vehicle details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkSaveVehicleDetailsRequest {

    @NotNull(message = "Vehicle details list is mandatory")
    @Valid
    private List<PdaEntryVehicleDetailRequest> vehicleDetails;

    private List<Long> deleteDetRowIds; // IDs to delete
}

