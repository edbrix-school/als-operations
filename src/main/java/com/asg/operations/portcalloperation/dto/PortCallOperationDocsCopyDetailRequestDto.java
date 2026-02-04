package com.asg.operations.portcalloperation.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortCallOperationDocsCopyDetailRequestDto {
    private Long transactionPoid;
    private Long detRowId;

    @Size(max = 100)
    private String documentFrom;

    @Size(max = 4000)
    private String documentList;

    @Size(max = 1)
    private String documentSelect;

    @NotNull(message = "Send Email is required")
    private Boolean sendEmail;

    private ActionType actionType;
}
