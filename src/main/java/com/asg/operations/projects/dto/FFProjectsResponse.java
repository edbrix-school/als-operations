package com.asg.operations.projects.dto;

import com.asg.common.lib.dto.LovGetListDto;
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
    private LovGetListDto quotationReferenceLov;
    private String projectDescription;
    private String billingTo;
    private LovGetListDto billingToLov;
    private Long billingPartyPoid;
    private LovGetListDto billingPartyLov;
    private Long projectCustomerPoid;
    private LovGetListDto projectCustomerLov;
    private Long principalPoid;
    private LovGetListDto principalLov;
    private String shipmentMode;
    private LovGetListDto shipmentModeLov;
    private String mode;
    private LovGetListDto modeLov;
    private String projectReference;
    private LocalDate periodFrom;
    private LocalDate periodTo;
    private Long salesmanPoid;
    private LovGetListDto salesmanLov;
    private Long linePoid;
    private LovGetListDto lineLov;
    private Long carrierCodePoid;
    private LovGetListDto carrierCodeLov;
    private String commodity;
    private String cargoDetails;
    private String billingCurrencyCode;
    private LovGetListDto billingCurrencyLov;
    private String projectStatus;
    private LovGetListDto projectStatusLov;
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
