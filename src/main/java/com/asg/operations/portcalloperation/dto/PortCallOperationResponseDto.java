package com.asg.operations.portcalloperation.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortCallOperationResponseDto {
    private Long transactionPoid;
    private LocalDate transactionDate;
    private Long groupPoid;
    private String docRef;
    private Long companyPoid;
    private Long vesselVoyagePoid;
    private String callSign;
    private String callType;
    private Long principalPoid;
    private Long vesselTypePoid;
    private String operatorName;
    private String chartererName;
    private String berth;
    private Long portOfCallPoid;
    private String agencyType;
    private String specialInstructions;
    private String termsConditions;
    private String pcInfoAttachments;
    private Long pdaRefPoid;
    private Long fdaRefPoid;
    private BigDecimal pdaAnchorageStayDays;
    private BigDecimal pdaBerthStayDays;
    private BigDecimal pdaPortStayDays;
    private String pdaFdaAttachments;
    private String pdaFdaRemarks;
    private String portCallActualTimingRemarks;
    private String husbandryCrewReqBy;
    private Long docsCopyEmailPoid;
    private Long grt;
    private Long nrt;
    private Long dwt;
    private String status;
    private List<PortCallOperationCargoDetailResponseDto> cargoDetails;
    private List<PortCallOperationMailDetailResponseDto> mailDetails;
    private List<PortCallOperationEstBertDetailResponseDto> estBertDetails;
    private List<PortCallOperationEstPrearrivalDetailResponseDto> estPrearrivalDetails;
    private List<PortCallOperationActTimingDetailResponseDto> actTimingDetails;
    private List<PortCallOperationActCondDetailResponseDto> actCondDetails;
    private List<PortCallOperationActRmksDetailResponseDto> actRmksDetails;
    private List<PortCallOperationActProgDetailResponseDto> actProgDetails;
    private List<PortCallOperationActCargoFigDetailResponseDto> actCargoFigDetails;
    private List<PortCallOperationActBunkerDetailResponseDto> actBunkerDetails;
    private List<PortCallOperationHusbandryCrewDetailResponseDto> husbandryCrewDetails;
    private List<PortCallOperationHusbandryOthDetailResponseDto> husbandryOthDetails;
    private List<PortCallOperationDocsCopyDetailResponseDto> docsCopyDetails;
    private List<PortCallOperationDocsMsgsDtl1DetailResponseDto> docsMsgsDtl1Details;
    private List<PortCallOperationDocsMsgsDtl2DetailResponseDto> docsMsgsDtl2Details;
}
