package com.asg.operations.projects.util;

import com.asg.common.lib.dto.LovGetListDto;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LovDataService;
import com.asg.operations.projectjob.entity.FFManifestHdr;
import com.asg.operations.projectjob.repository.FFManifestHdrRepository;
import com.asg.operations.projects.dto.*;
import com.asg.operations.projects.entity.FFProjectsChargesDtl;
import com.asg.operations.projects.entity.FFProjectsCtrlSheetDtl;
import com.asg.operations.projects.entity.FFProjectsHdr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectMapper {

    @Autowired
    private LovDataService lovDataService;

    @Autowired
    private FFManifestHdrRepository manifestHdrRepository;

    public static void applyUpdate(FFProjectsRequest request, FFProjectsHdr existingProjectsHdr) {
        existingProjectsHdr.setQuotationReferencePoid(request.getQuotationReferencePoid());
        existingProjectsHdr.setProjectDescription(request.getProjectDescription());
        existingProjectsHdr.setBillingTo(request.getBillingTo());
        existingProjectsHdr.setBillingPartyPoid(request.getBillingPartyPoid());
        existingProjectsHdr.setProjectCustomerPoid(request.getProjectCustomerPoid());
        existingProjectsHdr.setPrincipalPoid(request.getPrincipalPoid());
        existingProjectsHdr.setShipmentMode(request.getShipmentMode() != null ? String.join(",", request.getShipmentMode()) : null);
        existingProjectsHdr.setMode(request.getMode());
        existingProjectsHdr.setProjectReference(request.getProjectReference());
        existingProjectsHdr.setPeriodFrom(request.getPeriodFrom());
        existingProjectsHdr.setPeriodTo(request.getPeriodTo());
        existingProjectsHdr.setSalesmanPoid(request.getSalesmanPoid());
        existingProjectsHdr.setLinePoid(request.getLinePoid());
        existingProjectsHdr.setCarrierCodePoid(request.getCarrierCodePoid());
        existingProjectsHdr.setCommodity(String.join(",", request.getCommodity()));
        existingProjectsHdr.setCargoDetails(request.getCargoDetails());
        existingProjectsHdr.setBillingCurrencyCode(request.getBillingCurrencyCode());
        existingProjectsHdr.setProjectStatus(request.getProjectStatus());
        existingProjectsHdr.setAgreedRate(request.getAgreedRate());
        existingProjectsHdr.setLastModifiedBy(UserContext.getUserName());
        existingProjectsHdr.setLastModifiedDate(LocalDateTime.now());
    }

    public static FFProjectsCtrlSheetDtl createCtrlSheet(FFProjectsCtrlSheetDetailRequest ctrlReq, Long transactionPoid, long nextDetRowId) {
        return FFProjectsCtrlSheetDtl.builder()
                .transactionPoid(transactionPoid)
                .detRowId(nextDetRowId)
                .freightType(ctrlReq.getFreightType())
                .jobNoPoid(ctrlReq.getJobNoPoid())
                .origin(ctrlReq.getOriginPoid())
                .destination(ctrlReq.getDestinationPoid())
                .etd(ctrlReq.getEtd())
                .etaAta(ctrlReq.getEtaAta())
                .arrivalDate(ctrlReq.getArrivalDate())
                .noOfPackages(ctrlReq.getNoOfPackages())
                .weight(ctrlReq.getWeight())
                .cbm(ctrlReq.getCbm())
                .carrierPoid(ctrlReq.getCarrierPoid())
                .line(ctrlReq.getLinePoid())
                .truckNumber(ctrlReq.getTruckNumber())
                .description(ctrlReq.getDescription())
                .sailDate(ctrlReq.getSailDate())
                .pol(ctrlReq.getSfPOL())
                .pod(ctrlReq.getSfPOD())
                .lastModifiedBy(UserContext.getUserName())
                .lastModifiedDate(LocalDateTime.now())
                .createdBy(UserContext.getUserName())
                .createdDate(LocalDateTime.now())
                .build();
    }

    public static FFProjectsChargesDtl buildCreateCharge(FFProjectsChargesDetailRequest chargeReq, Long transactionPoid, long nextDetRowId) {
        return FFProjectsChargesDtl.builder()
                .transactionPoid(transactionPoid)
                .detRowId(nextDetRowId)
                .quotationReferencePoid(chargeReq.getQuotationReferencePoid())
                .chargeDetailsPoid(chargeReq.getChargePoid())
                .printableChargeDescription(chargeReq.getPrintableChargeDescription())
                .quantity(chargeReq.getQuantity())
                .unit(chargeReq.getUnit())
                .buyingCurrencyCode(chargeReq.getBuyingCurrencyCode())
                .currencyRate(chargeReq.getCurrencyRate())
                .buyingUnitRate(chargeReq.getBuyingUnitRate())
                .buyingTotalBhd(chargeReq.getBuyingTotalBhd())
                .sellingUnitRate(chargeReq.getSellingUnitRate())
                .sellingTotal(chargeReq.getSellingTotal())
                .taxIdPoid(chargeReq.getTaxIdPoid())
                .taxPercentage(chargeReq.getTaxPercentage())
                .taxAmount(chargeReq.getTaxAmount())
                .sellingGrandTotal(chargeReq.getSellingGrandTotal())
                .sellingGrandTotalBhd(chargeReq.getSellingGrandTotalBhd())
                .marginBhd(chargeReq.getMarginBhd())
                .remarks(chargeReq.getRemarks())
                .createdBy(UserContext.getUserName())
                .createdDate(LocalDateTime.now())
                .build();
    }

    public static FFProjectsHdr buildCreateProject(FFProjectsRequest request) {
        FFProjectsHdr projectsHdr = FFProjectsHdr.builder()
                .transactionDate(LocalDate.now())
                .companyPoid(UserContext.getCompanyPoid())
                .quotationReferencePoid(request.getQuotationReferencePoid())
                .projectDescription(request.getProjectDescription())
                .billingTo(request.getBillingTo())
                .billingPartyPoid(request.getBillingPartyPoid())
                .projectCustomerPoid(request.getProjectCustomerPoid())
                .principalPoid(request.getPrincipalPoid())
                .shipmentMode(request.getShipmentMode() != null ? String.join(",", request.getShipmentMode()) : null)
                .mode(request.getMode())
                .projectReference(request.getProjectReference())
                .periodFrom(request.getPeriodFrom())
                .periodTo(request.getPeriodTo())
                .salesmanPoid(request.getSalesmanPoid())
                .linePoid(request.getLinePoid())
                .carrierCodePoid(request.getCarrierCodePoid())
                .commodity(String.join(",", request.getCommodity()))
                .cargoDetails(request.getCargoDetails())
                .billingCurrencyCode(request.getBillingCurrencyCode())
                .projectStatus(request.getProjectStatus() != null ? request.getProjectStatus() : "Open")
                .agreedRate(request.getAgreedRate())
                .deleted("N")
                .createdBy(UserContext.getUserName())
                .createdDate(LocalDateTime.now())
                .build();
        return projectsHdr;
    }

    public FFProjectsResponse mapToResponse(FFProjectsHdr hdr, List<FFProjectsChargesDtl> chargeDetails, List<FFProjectsCtrlSheetDtl> ctrlSheetDetails) {
        List<FFProjectsChargesDetailResponse> chargeResponses = chargeDetails.stream()
                .map(this::mapChargeDetailToResponse)
                .collect(Collectors.toList());

        List<FFProjectsCtrlSheetDetailResponse> ctrlSheetResponses = ctrlSheetDetails.stream()
                .map(this::mapCtrlSheetDetailToResponse)
                .collect(Collectors.toList());

        return FFProjectsResponse.builder()
                .transactionPoid(hdr.getTransactionPoid())
                .transactionDate(hdr.getTransactionDate())
                .companyPoid(hdr.getCompanyPoid())
                .docRef(hdr.getDocRef())
                .quotationReferencePoid(hdr.getQuotationReferencePoid())
                .quotationReferenceLov(getLov(hdr.getQuotationReferencePoid(), "PROJECTS_QUOTATIONS"))
                .projectDescription(hdr.getProjectDescription())
                .billingTo(hdr.getBillingTo())
                .billingToLov(getLovByCode(hdr.getBillingTo(), "FF_BILLING_TO"))
                .billingPartyPoid(hdr.getBillingPartyPoid())
                .billingPartyLov(getLov(hdr.getBillingPartyPoid(), "CUSTOMER_SUPPLIER_MASTER"))
                .projectCustomerPoid(hdr.getProjectCustomerPoid())
                .projectCustomerLov(getLov(hdr.getProjectCustomerPoid(), "CUSTOMER_SUPPLIER_MASTER"))
                .principalPoid(hdr.getPrincipalPoid())
                .principalLov(getLov(hdr.getPrincipalPoid(), "PRINCIPAL_MASTER"))
                .shipmentMode(hdr.getShipmentMode() != null ? List.of(hdr.getShipmentMode().split(",")) : List.of())
                .shipmentModeLov(hdr.getShipmentMode() != null
                        ? List.of(hdr.getShipmentMode().split(",")).stream().map(code -> getLovByCode(code.trim(), "PROJECTS_SHIPMENT_MODE")).collect(Collectors.toList())
                        : List.of())
                .mode(hdr.getMode())
                .modeLov(getLovByCode(hdr.getMode(), "PROJECTS_MODE"))
                .projectReference(hdr.getProjectReference())
                .periodFrom(hdr.getPeriodFrom())
                .periodTo(hdr.getPeriodTo())
                .salesmanPoid(hdr.getSalesmanPoid())
                .salesmanLov(getLov(hdr.getSalesmanPoid(), "SALESMAN"))
                .linePoid(hdr.getLinePoid())
                .lineLov(getLov(hdr.getLinePoid(), "LINE_MASTER"))
                .carrierCodePoid(hdr.getCarrierCodePoid())
                .carrierCodeLov(getLov(hdr.getCarrierCodePoid(), "AIRLINE"))
                .commodity(hdr.getCommodity())
                .cargoDetails(hdr.getCargoDetails())
                .billingCurrencyCode(hdr.getBillingCurrencyCode())
                .billingCurrencyLov(getLovByCode(hdr.getBillingCurrencyCode(), "CURRENCY"))
                .projectStatus(hdr.getProjectStatus())
                .projectStatusLov(getLovByCode(hdr.getProjectStatus(), "PROJECTS_STATUS"))
                .agreedRate(hdr.getAgreedRate())
                .createdBy(hdr.getCreatedBy())
                .createdDate(hdr.getCreatedDate())
                .lastModifiedBy(hdr.getLastModifiedBy())
                .lastModifiedDate(hdr.getLastModifiedDate())
                .chargeDetails(chargeResponses)
                .controlSheetDetails(ctrlSheetResponses)
                .build();
    }

    public FFProjectsChargesDetailResponse mapChargeDetailToResponse(FFProjectsChargesDtl dtl) {
        return FFProjectsChargesDetailResponse.builder()
                .transactionPoid(dtl.getTransactionPoid())
                .detRowId(dtl.getDetRowId())
                .quotationReferencePoid(dtl.getQuotationReferencePoid())
                .quotationReferenceLov(getLov(dtl.getQuotationReferencePoid(), "PROJECTS_QUOTATIONS_SUPP"))
                .chargeDetailsPoid(dtl.getChargeDetailsPoid())
                .chargeDetailsLov(getLov(dtl.getChargeDetailsPoid(), "CHARGE_MASTER_FF"))
                .printableChargeDescription(dtl.getPrintableChargeDescription())
                .quantity(dtl.getQuantity())
                .unit(dtl.getUnit())
                .unitLov(getLov(Long.valueOf(dtl.getUnit()), "PROJECTS_CHARGES_UNIT"))
                .buyingCurrencyCode(dtl.getBuyingCurrencyCode())
                .buyingCurrencyLov(getLovByCode(dtl.getBuyingCurrencyCode(), "CURRENCY"))
                .currencyRate(dtl.getCurrencyRate())
                .buyingUnitRate(dtl.getBuyingUnitRate())
                .buyingTotalBhd(dtl.getBuyingTotalBhd())
                .sellingUnitRate(dtl.getSellingUnitRate())
                .sellingTotal(dtl.getSellingTotal())
                .taxIdPoid(dtl.getTaxIdPoid())
                .taxIdLov(getLov(dtl.getTaxIdPoid(), "TAX_MASTER"))
                .taxPercentage(dtl.getTaxPercentage())
                .taxAmount(dtl.getTaxAmount())
                .sellingGrandTotal(dtl.getSellingGrandTotal())
                .sellingGrandTotalBhd(dtl.getSellingGrandTotalBhd())
                .marginBhd(dtl.getMarginBhd())
                .remarks(dtl.getRemarks())
                .createdBy(dtl.getCreatedBy())
                .createdDate(dtl.getCreatedDate())
                .lastModifiedBy(dtl.getLastModifiedBy())
                .lastModifiedDate(dtl.getLastModifiedDate())
                .build();
    }

    public FFProjectsCtrlSheetDetailResponse mapCtrlSheetDetailToResponse(FFProjectsCtrlSheetDtl dtl) {
        String jobNo = null;
        if (dtl.getJobNoPoid() != null) {
            jobNo = manifestHdrRepository.findById(dtl.getJobNoPoid())
                    .map(FFManifestHdr::getFfJobNo)
                    .orElse(null);
        }
        return FFProjectsCtrlSheetDetailResponse.builder()
                .transactionPoid(dtl.getTransactionPoid())
                .detRowId(dtl.getDetRowId())
                .freightType(dtl.getFreightType())
                .jobNoPoid(dtl.getJobNoPoid())
                .jobNo(jobNo)
                .originPoid(dtl.getOrigin())
                .originLov(getLov(dtl.getOrigin(), "FF_AIRPORTS"))
                .destinationPoid(dtl.getDestination())
                .destinationLov(getLov(dtl.getDestination(), "FF_AIRPORTS"))
                .etd(dtl.getEtd())
                .etaAta(dtl.getEtaAta())
                .arrivalDate(dtl.getArrivalDate())
                .noOfPackages(dtl.getNoOfPackages())
                .weight(dtl.getWeight())
                .cbm(dtl.getCbm())
                .carrierPoid(dtl.getCarrierPoid())
                .carrierLov(getLov(dtl.getCarrierPoid(), "AIRLINE"))
                .linePoid(dtl.getLine())
                .lineLov(getLov(dtl.getLine(), "LINE_MASTER"))
                .truckNumber(dtl.getTruckNumber())
                .description(dtl.getDescription())
                .sailDate(dtl.getSailDate())
                .createdBy(dtl.getCreatedBy())
                .createdDate(dtl.getCreatedDate())
                .lastModifiedBy(dtl.getLastModifiedBy())
                .lastModifiedDate(dtl.getLastModifiedDate())
                .build();
    }

    private LovGetListDto getLov(Long poid, String lovName) {
        if (poid == null) return null;
        return lovDataService.getDetailsByPoidAndLovNameFast(poid, lovName);
    }

    private LovGetListDto getLovByCode(String code, String lovName) {
        if (code == null || code.isEmpty()) return null;
        return lovDataService.getLovItemByCodeFast(code, lovName);
    }

    public FFProjectsListResponse mapToListResponse(FFProjectsHdr hdr) {
        return FFProjectsListResponse.builder()
                .transactionPoid(hdr.getTransactionPoid())
                .transactionDate(hdr.getTransactionDate())
                .docRef(hdr.getDocRef())
                .projectDescription(hdr.getProjectDescription())
                .projectStatus(hdr.getProjectStatus())
                .periodFrom(hdr.getPeriodFrom())
                .periodTo(hdr.getPeriodTo())
                .build();
    }
}
