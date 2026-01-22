package com.asg.operations.portcalloperation.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationDocsCopyDetailDto {
    private Long transactionPoid;
    private Long detRowId;

    @Size(max = 100)
    private String documentFrom;

    @Size(max = 4000)
    private String documentList;

    @Size(max = 1)
    private String documentSelect;

    @Size(max = 4000)
    @NotBlank(message = "Document Attachments cannot be empty")
    private String documentAttachments;

    private ActionType actionType;
}
