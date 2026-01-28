package com.asg.operations.portcalloperation.dto;

import com.asg.common.lib.dto.LovGetListDto;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private LovGetListDto groupDet;
    private String docRef;
    private Long companyPoid;
    private LovGetListDto companyDet;
    private Long vesselVoyagePoid;
    private LovGetListDto vesselVoyageDet;
    private String callSign;
    private String callType;
    private Long principalPoid;
    private LovGetListDto principalDet;
    private Long vesselTypePoid;
    private LovGetListDto vesselTypeDet;
    private String operatorName;
    private String chartererName;
    private String berth;
    private LovGetListDto berthDet;
    private Long portOfCallPoid;
    private LovGetListDto portOfCallDet;
    private String agencyType;
    private String specialInstructions;
    private String termsConditions;
    private String pcInfoAttachments;
    private Long pdaRefPoid;
    private LovGetListDto pdaRefDet;
    private Long fdaRefPoid;
    private LovGetListDto fdaRefDet;
    private BigDecimal pdaAnchorageStayDays;
    private BigDecimal pdaBerthStayDays;
    private BigDecimal pdaPortStayDays;
    private String pdaFdaAttachments;
    private String pdaFdaRemarks;
    private String portCallActualTimingRemarks;
    private String husbandryCrewReqBy;
    private Long docsCopyEmailPoid;
    private LovGetListDto docsCopyEmailDet;
    private Long grt;
    private Long nrt;
    private Long dwt;
    private String status;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
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
