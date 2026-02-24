package com.asg.operations.projects.util;

import com.asg.common.lib.security.util.UserContext;
import com.asg.operations.projects.dto.*;
import com.asg.operations.projects.entity.FFProjectsChargesDtl;
import com.asg.operations.projects.entity.FFProjectsCtrlSheetDtl;
import com.asg.operations.projects.entity.FFProjectsHdr;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectMapper {

    public static void applyUpdate(FFProjectsRequest request, FFProjectsHdr existingProjectsHdr) {
        existingProjectsHdr.setQuotationReferencePoid(request.getQuotationReferencePoid());
        existingProjectsHdr.setProjectDescription(request.getProjectDescription());
        existingProjectsHdr.setBillingTo(request.getBillingTo());
        existingProjectsHdr.setBillingPartyPoid(request.getBillingPartyPoid());
        existingProjectsHdr.setProjectCustomerPoid(request.getProjectCustomerPoid());
        existingProjectsHdr.setPrincipalPoid(request.getPrincipalPoid());
        existingProjectsHdr.setShipmentMode(request.getShipmentMode());
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
                .shipmentMode(request.getShipmentMode())
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
                .projectDescription(hdr.getProjectDescription())
                .billingTo(hdr.getBillingTo())
                .billingPartyPoid(hdr.getBillingPartyPoid())
                .projectCustomerPoid(hdr.getProjectCustomerPoid())
                .principalPoid(hdr.getPrincipalPoid())
                .shipmentMode(hdr.getShipmentMode())
                .mode(hdr.getMode())
                .projectReference(hdr.getProjectReference())
                .periodFrom(hdr.getPeriodFrom())
                .periodTo(hdr.getPeriodTo())
                .salesmanPoid(hdr.getSalesmanPoid())
                .linePoid(hdr.getLinePoid())
                .carrierCodePoid(hdr.getCarrierCodePoid())
                .commodity(hdr.getCommodity())
                .cargoDetails(hdr.getCargoDetails())
                .billingCurrencyCode(hdr.getBillingCurrencyCode())
                .projectStatus(hdr.getProjectStatus())
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
                .chargeDetailsPoid(dtl.getChargeDetailsPoid())
                .printableChargeDescription(dtl.getPrintableChargeDescription())
                .quantity(dtl.getQuantity())
                .unit(dtl.getUnit())
                .buyingCurrencyCode(dtl.getBuyingCurrencyCode())
                .currencyRate(dtl.getCurrencyRate())
                .buyingUnitRate(dtl.getBuyingUnitRate())
                .buyingTotalBhd(dtl.getBuyingTotalBhd())
                .sellingUnitRate(dtl.getSellingUnitRate())
                .sellingTotal(dtl.getSellingTotal())
                .taxIdPoid(dtl.getTaxIdPoid())
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
        return FFProjectsCtrlSheetDetailResponse.builder()
                .transactionPoid(dtl.getTransactionPoid())
                .detRowId(dtl.getDetRowId())
                .freightType(dtl.getFreightType())
                .jobNoPoid(dtl.getJobNoPoid())
                .originPoid(dtl.getOrigin())
                .destinationPoid(dtl.getDestination())
                .etd(dtl.getEtd())
                .etaAta(dtl.getEtaAta())
                .arrivalDate(dtl.getArrivalDate())
                .noOfPackages(dtl.getNoOfPackages())
                .weight(dtl.getWeight())
                .cbm(dtl.getCbm())
                .carrierPoid(dtl.getCarrierPoid())
                .linePoid(dtl.getLine())
                .truckNumber(dtl.getTruckNumber())
                .description(dtl.getDescription())
                .sailDate(dtl.getSailDate())
                .createdBy(dtl.getCreatedBy())
                .createdDate(dtl.getCreatedDate())
                .lastModifiedBy(dtl.getLastModifiedBy())
                .lastModifiedDate(dtl.getLastModifiedDate())
                .build();
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
