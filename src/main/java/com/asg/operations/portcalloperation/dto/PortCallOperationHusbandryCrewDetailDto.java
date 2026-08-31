package com.asg.operations.portcalloperation.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import jakarta.validation.constraints.Size;
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
public class PortCallOperationHusbandryCrewDetailDto {
    private Long transactionPoid;
    private Long detRowId;

    @Size(max = 1000, message = "Crew Name should not exceed 1000 characters")
    private String crewName;

    private Long crewGenderPoid;
    private Long crewNationalityPoid;

    @Size(max = 300, message = "PPT Number should not exceed 300 characters")
    private String crewPptNumber;

    @Size(max = 300, message = "Seaman Number should not exceed 300 characters")
    private String crewSeamanNo;

    @Size(max = 100, message = "Crew Rank should not exceed 100 characters")
    private String crewRank;

    @Size(max = 4000, message = "Crew Attachments should not exceed 4000 characters")
    private String crewAttachments;

    @Size(max = 50, message = "Crew Sign Status should not exceed 50 characters")
    private String crewSignStatus;

    private ActionType actionType;
}
