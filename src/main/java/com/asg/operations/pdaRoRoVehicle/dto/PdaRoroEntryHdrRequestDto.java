package com.asg.operations.pdaRoRoVehicle.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdaRoroEntryHdrRequestDto {

    //private LocalDate transactionDate;

    @NotNull(message = "Vessel Voyage is mandatory")
    private Long vesselVoyagePoid;

    /*private String vesselName;

    private String voyageNo;*/

    private String remarks;
}
