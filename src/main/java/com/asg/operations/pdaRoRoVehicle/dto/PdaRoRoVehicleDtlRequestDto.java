package com.asg.operations.pdaRoRoVehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdaRoRoVehicleDtlRequestDto {

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
    private String actionType;
}
