package com.asg.operations.pdaRoRoVehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdaRoRoVehicleDtlResponseDto {

    private Long detRowId;
    private String blNumber;
    private String shipper;
    private String consignee;
    private String vinNumber;
    private String description;
    private Double blGwt;
    private Double blCbm;
    private String portOfLoad;
    private String agent;
    private String createBy;
    private String lastModifyBy;
    private LocalDateTime createDate;
    private LocalDateTime lastModifyDate;
}
