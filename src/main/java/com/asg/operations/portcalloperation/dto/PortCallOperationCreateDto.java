package com.asg.operations.portcalloperation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationCreateDto {
    @NotNull(message = "Vessel Voyage Poid is required")
    private Long vesselVoyagePoid;

    @Size(max = 100)
    private String callSign;

    @Size(max = 100)
    @NotNull(message = "Call Type is required")
    private String callType;

    @NotNull(message = "Principal Poid is required")
    private Long principalPoid;

    private Long grt;

    private Long nrt;

    private Long dwt;

    private String operatorName;

    private String chartererName;

    @Size(max = 100)
    private String berth;

    @NotNull(message = "Port of Call Poid is required")
    private Long portOfCallPoid;


    @Valid
    private List<PortCallOperationCargoDetailDto> cargoDetails;

    @Valid
    private List<PortCallOperationMailDetailDto> mailDetails;
}
