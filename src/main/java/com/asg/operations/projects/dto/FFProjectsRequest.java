package com.asg.operations.projects.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FFProjectsRequest {

    private Long quotationReferencePoid;

    private String projectDescription;

    private String billingTo;

    private Long billingPartyPoid;

    private Long projectCustomerPoid;

    private Long principalPoid;

    private List<String> shipmentMode;

    private String mode;

    private String projectReference;

    private LocalDate periodFrom;

    private LocalDate periodTo;

    private Long salesmanPoid;

    private Long linePoid;

    private Long carrierCodePoid;

    private List<String> commodity;

    private String cargoDetails;

    private String billingCurrencyCode;

    private Double agreedRate;

    private String projectStatus = "OPEN";

    @Valid
    private List<FFProjectsChargesDetailRequest> chargeDetails;

    @Valid
    private List<FFProjectsCtrlSheetDetailRequest> controlSheetDetails;
}
