package com.asg.operations.pdaentryform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkSaveChargeDetailsRequest {

    @NotNull(message = "Charge details list is mandatory")
    @Valid
    private List<PdaEntryChargeDetailRequest> chargeDetails;

    private List<Long> deleteDetRowIds; // IDs to delete
}