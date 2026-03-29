package com.asg.operations.portcalloperation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationCreateDto {
    @NotNull(message = "Vessel Voyage Poid is required")
    private Long vesselVoyagePoid;

    @NotNull(message = "Transaction Date is required")
    private LocalDate transactionDate;

    @Size(max = 100)
    private String callSign;

    @Size(max = 100)
    @NotNull(message = "Call Type is required")
    private String callType;

    @NotNull(message = "Principal Poid is required")
    private Long principalPoid;

    private Long grt;

    private Long nrt;

    private Long dwt;

    private String operatorName;

    private String chartererName;

    @Size(max = 100)
    private String berth;

    @NotNull(message = "Port of Call Poid is required")
    private Long portOfCallPoid;

    @Size(max = 4000, message = "Special Instructions should not exceed 4000 characters")
    private String specialInstructions;

    @Size(max = 4000, message = "Terms and Conditions should not exceed 4000 characters")
    private String termsConditions;

    @Valid
    private List<PortCallOperationCargoDetailDto> cargoDetails;

    @Valid
    @NotEmpty(message = "mailDetails must contain at least one item")
    private List<PortCallOperationMailDetailDto> mailDetails;
}
