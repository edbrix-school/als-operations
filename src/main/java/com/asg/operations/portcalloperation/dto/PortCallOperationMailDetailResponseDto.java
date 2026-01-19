package com.asg.operations.portcalloperation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationMailDetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private String communicationType;
    private String communicationMode;
    private String company;
    private String addressee;
    private String emailIds;
}
