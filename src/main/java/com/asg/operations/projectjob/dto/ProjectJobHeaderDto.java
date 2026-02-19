package com.asg.operations.projectjob.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ProjectJobHeaderDto {

    private Long transactionPoid;
    private Long projectPoid;
    private Long billingTo;
    private Long billToCustomerPoid;
    private Long principalPoid;
    private Long salesmanPoid;

    private String shipmentMode;
    private String transportationMode;

    private String jobStatus;
    private LocalDate jobClosedDate;

    private String cargoDescription;

}