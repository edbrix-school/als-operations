package com.asg.operations.portcalloperation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationDto {
    private Long transactionPoid;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    private String docRef;
    @NotNull(message = "Vessel Voyage Poid is required")
    private Long vesselVoyagePoid;

    @Size(max = 100)
    private String callSign;

    @Size(max = 100)
    @NotNull(message = "Call Type is required")
    private String callType;

    @NotNull(message = "Principal Poid is required")
    private Long principalPoid;
    private Long vesselTypePoid;

    @Size(max = 300)
    private String operatorName;

    @Size(max = 300)
    private String chartererName;

    @Size(max = 100)
    private String berth;

    private Long portOfCallPoid;

    @Size(max = 300)
    private String agencyType;

    @Size(max = 4000)
    private String specialInstructions;

    @Size(max = 4000)
    private String termsConditions;

    @Size(max = 4000)
    private String pcInfoAttachments;

    private Long pdaRefPoid;
    private Long fdaRefPoid;
    private BigDecimal pdaAnchorageStayDays;
    private BigDecimal pdaBerthStayDays;
    private BigDecimal pdaPortStayDays;

    @Size(max = 4000)
    private String pdaFdaAttachments;

    @Size(max = 1000)
    private String pdaFdaRemarks;

    @Size(max = 1000)
    private String portCallActualTimingRemarks;

    @Size(max = 100)
    private String husbandryCrewReqBy;

    private Long docsCopyEmailPoid;

    @Size(max = 100)
    private String status;

    @Valid
    private List<PortCallOperationCargoDetailDto> cargoDetails;

    @Valid
    private List<PortCallOperationMailDetailDto> mailDetails;

    @Valid
    private List<PortCallOperationEstBertDetailDto> estBertDetails;

    @Valid
    private List<PortCallOperationEstPrearrivalDetailDto> estPrearrivalDetails;

    @Valid
    private List<PortCallOperationEstPrearrivalActDetailDto> estPrearrivalActDetails;

    @Valid
    private List<PortCallOperationActTimingDetailDto> actTimingDetails;

    @Valid
    private List<PortCallOperationActTimingsActvtyDetailDto> actTimingsActvtyDetails;

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
