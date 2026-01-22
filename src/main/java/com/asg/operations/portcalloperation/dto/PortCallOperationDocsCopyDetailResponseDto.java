package com.asg.operations.portcalloperation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationDocsCopyDetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private String documentFrom;
    private String documentList;
    private String documentSelect;
    private String documentAttachments;
}
