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
public class PortCallOperationDocsMsgsDtl2DetailDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long emailPoid;

    @Size(max = 300)
    private String emailType;

    @Size(max = 300)
    private String company;

    @Size(max = 300)
    private String addressee;

    @Size(max = 1000)
    private String toEmailId;

    @Size(max = 1000)
    private String ccEmailId;

    private ActionType actionType;
}
