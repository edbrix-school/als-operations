package com.asg.operations.projects.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FFProjectsResponse {

    private Long transactionPoid;
    private LocalDate transactionDate;
    private Long companyPoid;
    private String docRef;
    private Long quotationReferencePoid;
    private String projectDescription;
    private String billingTo;
    private Long billingPartyPoid;
    private Long projectCustomerPoid;
    private Long principalPoid;
    private String shipmentMode;
    private String mode;
    private String projectReference;
    private LocalDate periodFrom;
    private LocalDate periodTo;
    private Long salesmanPoid;
    private Long linePoid;
    private Long carrierCodePoid;
    private String commodity;
    private String cargoDetails;
    private String billingCurrencyCode;
    private String projectStatus;
    private Double totalBuyingRateBhd;
    private Double totalVatBhd;
    private Double grandTotalSellRateBhd;
    private Double grandTotalSellRateFc;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private List<FFProjectsChargesDetailResponse> chargeDetails;
    private List<FFProjectsCtrlSheetDetailResponse> controlSheetDetails;
}
