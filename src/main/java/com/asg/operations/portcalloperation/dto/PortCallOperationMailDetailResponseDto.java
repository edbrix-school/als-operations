package com.asg.operations.portcalloperation.dto;

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
public class PortCallOperationMailDetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private String communicationType;
    private String communicationMode;
    private String company;
    private String addressee;
    private String emailIds;
}
