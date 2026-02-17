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

    @NotBlank(message = "Project description is required")
    private String projectDescription;

    @NotBlank(message = "Billing to is required")
    private String billingTo;

    @NotNull(message = "Billing party is required")
    private Long billingPartyPoid;

    @NotNull(message = "Project customer is required")
    private Long projectCustomerPoid;

    @NotNull(message = "Principal is required")
    private Long principalPoid;

    @NotBlank(message = "Shipment mode is required")
    private String shipmentMode;

    @NotBlank(message = "Mode is required")
    private String mode;

    private String projectReference;

    private LocalDate periodFrom;

    private LocalDate periodTo;

    @NotNull(message = "Salesman is required")
    private Long salesmanPoid;

    private Long linePoid;

    private Long carrierCodePoid;

    private String commodity;

    private String cargoDetails;

    private String billingCurrencyCode;

    private String projectStatus;

    @Valid
    private List<FFProjectsChargesDetailRequest> chargeDetails;

    @Valid
    private List<FFProjectsCtrlSheetDetailRequest> controlSheetDetails;
}
