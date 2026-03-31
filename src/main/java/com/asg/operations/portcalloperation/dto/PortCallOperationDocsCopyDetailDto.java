package com.asg.operations.portcalloperation.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortCallOperationDocsCopyDetailDto {
    private Long transactionPoid;
    private Long detRowId;

    @Size(max = 100, message = "Document From should not exceed 100 characters")
    private String documentFrom;

    @Size(max = 4000, message = "Document List should not exceed 4000 characters")
    private String documentList;

    @Size(max = 1, message = "Document Select should not exceed 1 character")
    private String documentSelect;

    @Size(max = 4000, message = "Document Attachments should not exceed 4000 characters")
    @NotBlank(message = "Document Attachments cannot be empty")
    private String documentAttachments;

    private ActionType actionType;
}
