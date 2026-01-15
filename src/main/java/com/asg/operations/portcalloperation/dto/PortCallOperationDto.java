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

    @Size(max = 4000, message = "PC Info Attachments should not exceed 4000 characters")
    private String pcInfoAttachments;

    private Long pdaRefPoid;

    private Long fdaRefPoid;

    private BigDecimal pdaAnchorageStayDays;

    private BigDecimal pdaBerthStayDays;

    private BigDecimal pdaPortStayDays;

    @Size(max = 1000, message = "PDA FDA Remarks should not exceed 1000 characters")
    private String pdaFdaRemarks;

    @Size(max = 4000, message = "PDA FDA Attachments should not exceed 4000 characters")
    private String pdaFdaAttachments;

    @Size(max = 100,message = "Husbandry Crew Req By should not exceed 100 characters")
    private String husbandryCrewReqBy;

    @Valid
    private List<PortCallOperationCargoDetailDto> cargoDetails;

    @Valid
    @NotEmpty(message = "mailDetails must contain at least one item")
    private List<PortCallOperationMailDetailDto> mailDetails;

    @Valid
    private List<PortCallOperationEstBertDetailDto> estBertDetails;

    @Valid
    private List<PortCallOperationEstPrearrivalDetailDto> estPrearrivalDetails;

    @Valid
    private List<PortCallOperationActTimingDetailDto> actTimingDetails;

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

    @Valid
    private List<PortCallOperationDocsMsgsDtl1DetailDto> docsMsgsDtl1Details;

    @Valid
    private List<PortCallOperationDocsMsgsDtl2DetailDto> docsMsgsDtl2Details;
}
