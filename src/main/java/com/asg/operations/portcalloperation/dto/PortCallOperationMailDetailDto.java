package com.asg.operations.portcalloperation.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import jakarta.validation.constraints.NotNull;
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
public class PortCallOperationMailDetailDto {
    private Long transactionPoid;
    private Long detRowId;

    @NotNull(message = "Communication Type is required")
    @Size(max = 100)
    private String communicationType;

    @Size(max = 100)
    private String communicationMode;

    @Size(max = 300)
    private String company;

    @Size(max = 100)
    private String addressee;

    @Size(max = 1000)
    private String emailIds;

    private ActionType actionType;
}
