package com.asg.operations.portcalloperation.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationHusbandryCrewDetailDto {
    private Long transactionPoid;
    private Long detRowId;

    @Size(max = 1000)
    private String crewName;

    private Long crewGenderPoid;
    private Long crewNationalityPoid;

    @Size(max = 300)
    private String crewPptNumber;

    @Size(max = 300)
    private String crewSeamanNo;

    @Size(max = 100)
    private String crewRank;

    @Size(max = 4000)
    private String crewAttachments;

    private ActionType actionType;
}
