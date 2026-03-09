package com.asg.operations.pdaRoRoVehicle.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdaRoroEntryHdrRequestDto {

    private LocalDate transactionDate;

    @NotNull(message = "Vessel Voyage is mandatory")
    private Long vesselVoyagePoid;

    private String remarks;
}
