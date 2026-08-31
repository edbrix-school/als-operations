package com.asg.operations.portcalloperation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationHusbandryCrewDetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private String crewName;
    private Long crewGenderPoid;
    private Long crewNationalityPoid;
    private String crewPptNumber;
    private String crewSeamanNo;
    private String crewRank;
    private String crewAttachments;
    private String crewSignStatus;
}
