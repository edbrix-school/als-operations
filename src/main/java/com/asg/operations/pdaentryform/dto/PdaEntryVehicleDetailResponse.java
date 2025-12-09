package com.asg.operations.pdaentryform.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for PDA Entry Vehicle Detail
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdaEntryVehicleDetailResponse {

    private Long transactionPoid;
    private Long detRowId;
    private String vesselName;
    private String voyageRef;
    private String inOutMode;
    private String vehicleModel;
    private String vinNumber;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scanDate;

    private String damage;
    private String status;
    private String publishForImport;
    private String remarks;
    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;

    private String lastModifiedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastModifiedDate;

}

