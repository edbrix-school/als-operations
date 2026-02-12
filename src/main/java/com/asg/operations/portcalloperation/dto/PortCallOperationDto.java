package com.asg.operations.portcalloperation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationDto {
    @NotNull(message = "Vessel Voyage Poid is required")
    private Long vesselVoyagePoid;

    @Size(max = 100, message = "Call Sign should not exceed 100 characters")
    private String callSign;

    @Size(max = 100, message = "Call Type should not exceed 100 characters")
    @NotNull(message = "Call Type is required")
    private String callType;

    @NotNull(message = "Principal Poid is required")
    private Long principalPoid;

    @Size(max = 300, message = "Operator Name should not exceed 300 characters")
    private String operatorName;

    @Size(max = 300, message = "Charterer Name should not exceed 300 characters")
    private String chartererName;

    @Size(max = 100, message = "Berth should not exceed 100 characters")
    private String berth;

    private Long portOfCallPoid;

    private Long grt;

    private Long nrt;

    private Long dwt;

    @Size(max = 4000, message = "Special Instructions should not exceed 4000 characters")
    private String specialInstructions;

    @Size(max = 4000, message = "Terms and Conditions should not exceed 4000 characters")
    private String termsConditions;

    private Long pdaRefPoid;

    private Long fdaRefPoid;

    private BigDecimal pdaAnchorageStayDays;

    private BigDecimal pdaBerthStayDays;

    private BigDecimal pdaPortStayDays;

    @Size(max = 1000, message = "PDA FDA Remarks should not exceed 1000 characters")
    private String pdaFdaRemarks;

    @Valid
    private List<PortCallOperationCargoDetailDto> cargoDetails;

    @Valid
    @NotEmpty(message = "mailDetails must contain at least one item")
    private List<PortCallOperationMailDetailDto> mailDetails;

    @Valid
    private List<PortCallOperationActCondDetailDto> actCondDetails;

    @Valid
    private List<PortCallOperationActRmksDetailDto> actRmksDetails;

    @Valid
    private List<PortCallOperationActProgDetailDto> actProgDetails;

    @Valid
    private List<PortCallOperationActCargoFigDetailDto> actCargoFigDetails;

    @Valid
    private List<PortCallOperationActBunkerDetailDto> actBunkerDetails;

    @Valid
    private List<PortCallOperationHusbandryCrewDetailDto> husbandryCrewDetails;

    @Valid
    private List<PortCallOperationHusbandryOthDetailDto> husbandryOthDetails;

    @Valid
    private List<PortCallOperationDocsCopyDetailDto> docsCopyDetails;
}
