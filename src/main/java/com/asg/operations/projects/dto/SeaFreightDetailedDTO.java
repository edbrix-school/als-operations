package com.asg.operations.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeaFreightDetailedDTO {
    private SeaControlSheetDTO controlSheetInfo;
    private String feederVesselName;
    private String feederVoyageNo;
    private Long feederLoadPort;
    private Long feederUnloadPort;
    private LocalDateTime feederSailDate;
    private LocalDateTime feederEta;
    private LocalDateTime feederArrivalDate;
    private String motherVesselName;
    private String motherVoyageNo;
    private LocalDateTime motherSailDate;
    private LocalDateTime motherEta;
    private Long motherLoadPort;
    private Long motherUnloadPort;
    private Long motherTranshipPort;
    private String motherFinalDelv;
    private String masterBlNo;
    private String houseBlNo;
    private String houseBlNo2;
    private String blStatus;
    private LocalDateTime blIssueDate;
    private String releasedType;
    private String ofoqManifestRef;
    private String canPrinted;
    private String doPrinted;
    private String mablPrinted;
    private String radioActive;
    private List<ContainerDTO> containers;
    private List<BayanDTO> bayanDetails;
    private JobChargesDTO charges;
}
