package com.asg.operations.portcalloperation.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import jakarta.validation.constraints.NotBlank;
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
    @Size(max = 100, message = "Communication Type should not exceed 100 characters")
    private String communicationType;

    @Size(max = 100, message = "Communication Mode should not exceed 100 characters")
    private String communicationMode;

    @Size(max = 300, message = "Company should not exceed 300 characters")
    private String company;

    @Size(max = 100, message = "Addressee should not exceed 100 characters")
    private String addressee;

    @NotBlank(message = "Email Ids is required")
    @Size(max = 1000, message = "Email Ids should not exceed 1000 characters")
    private String emailIds;

    private ActionType actionType;
}
