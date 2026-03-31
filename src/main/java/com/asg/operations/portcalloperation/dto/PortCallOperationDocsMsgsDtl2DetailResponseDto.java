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
public class PortCallOperationDocsMsgsDtl2DetailResponseDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long emailPoid;
    private String emailType;
    private String company;
    private String addressee;
    private String toEmailId;
    private String ccEmailId;
}
