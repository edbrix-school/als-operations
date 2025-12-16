package com.asg.operations.pdaRoRoVehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdaRoroEntryHdrResponseDto {
    private Long transactionPoid;
    private String docRef;
    private LocalDate transactionDate;

    private Long vesselVoyagePoid;
    private String vesselName;
    private String voyageNo;
    private String remarks;
    private String createBy;
    private String lastModifyBy;
    private LocalDateTime createDate;
    private LocalDateTime lastModifyDate;

    private List<PdaRoroVehicleDtlResponseDto> vehicleDetails;
}
