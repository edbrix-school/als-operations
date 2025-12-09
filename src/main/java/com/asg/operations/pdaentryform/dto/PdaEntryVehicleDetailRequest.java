package com.asg.operations.pdaentryform.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdaEntryVehicleDetailRequest {

    private Long detRowId; // null for new, existing value for update

    @Size(max = 100)
    private String vesselName;

    @Size(max = 50)
    private String voyageRef;

    @Size(max = 5)
    private String inOutMode;

    @Size(max = 100)
    private String vehicleModel;

    @Size(max = 100)
    private String vinNumber;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scanDate;

    @Size(max = 5)
    private String damage;

    @Size(max = 50)
    private String status;

    @Size(max = 1)
    private String publishForImport;

    @Size(max = 500)
    private String remarks;
}
