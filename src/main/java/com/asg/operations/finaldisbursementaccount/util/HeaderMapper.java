package com.asg.operations.finaldisbursementaccount.util;

import com.asg.operations.finaldisbursementaccount.dto.FdaHeaderDto;
import com.asg.operations.finaldisbursementaccount.dto.UpdateFdaHeaderRequest;
import com.asg.operations.finaldisbursementaccount.entity.PdaFdaHdr;

import java.time.LocalDateTime;

public class HeaderMapper {

    public static FdaHeaderDto mapHeaderEntityToDto(PdaFdaHdr entity) {
        FdaHeaderDto dto = new FdaHeaderDto();

        dto.setTransactionPoid(entity.getTransactionPoid());
        dto.setTransactionDate(entity.getTransactionDate());
        dto.setGroupPoid(entity.getGroupPoid());
        dto.setCompanyPoid(entity.getCompanyPoid());
        dto.setPrincipalPoid(entity.getPrincipalPoid());
        dto.setPrincipalContact(entity.getPrincipalContact());
        dto.setDocRef(entity.getDocRef());
        dto.setVoyagePoid(entity.getVoyagePoid());
        dto.setVesselPoid(entity.getVesselPoid());
        dto.setArrivalDate(entity.getArrivalDate());
        dto.setSailDate(entity.getSailDate());
        dto.setPortPoid(entity.getPortPoid());
        dto.setCommodityPoid(entity.getCommodityPoid());
        dto.setOperationType(entity.getOperationType());
        dto.setImportQty(entity.getImportQty());
        dto.setExportQty(entity.getExportQty());
        dto.setTotalQuantity(entity.getTotalQuantity());
        dto.setUnit(entity.getUnit());
        dto.setHarbourCallType(entity.getHarbourCallType());
        dto.setCurrencyCode(entity.getCurrencyCode());
        dto.setCurrencyRate(entity.getCurrencyRate());
        dto.setCostCentrePoid(entity.getCostCentrePoid());
        dto.setVesselVerified(entity.getVesselVerified());
        dto.setVesselVerifiedDate(entity.getVesselVerifiedDate());
        dto.setVesselVerifiedBy(entity.getVesselVerifiedBy());
        dto.setUrgentApproval(entity.getUrgentApproval());
        dto.setPrincipalAprvlDays(entity.getPrincipalAprvlDays());
        dto.setPrincipalApproved(entity.getPrincipalApproved());
        dto.setPrincipalApprovedDate(entity.getPrincipalApprovedDate());
        dto.setPrincipalApprovedBy(entity.getPrincipalApprovedBy());
        dto.setReminderMinutes(entity.getReminderMinutes());
        dto.setCargoDetails(entity.getCargoDetails());
        dto.setStatus(entity.getStatus());
        dto.setFdaClosedDate(entity.getFdaClosedDate());
        dto.setRemarks(entity.getRemarks());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setLastModifiedBy(entity.getLastModifiedBy());
        dto.setLastModifiedDate(entity.getLastModifiedDate());
        dto.setDeleted(entity.getDeleted());
        dto.setPdaRef(entity.getPdaRef());
        dto.setAddressPoid(entity.getAddressPoid());
        dto.setSalesmanPoid(entity.getSalesmanPoid());
        dto.setTranshipmentQty(entity.getTranshipmentQty());
        dto.setDwt(entity.getDwt());
        dto.setGrt(entity.getGrt());
        dto.setImoNumber(entity.getImoNumber());
        dto.setNrt(entity.getNrt());
        dto.setNumberOfDays(entity.getNumberOfDays());
        dto.setPortDescription(entity.getPortDescription());
        dto.setTermsPoid(entity.getTermsPoid());
        dto.setVesselTypePoid(entity.getVesselTypePoid());
        dto.setLinePoid(entity.getLinePoid());
        dto.setPrintPrincipal(entity.getPrintPrincipal());
        dto.setVoyageNo(entity.getVoyageNo());
        dto.setProfitLossAmount(entity.getProfitLossAmount());
        dto.setProfitLossPer(entity.getProfitLossPer());
        dto.setFdaClosingBy(entity.getFdaClosingBy());
        dto.setGlClosingDate(entity.getGlClosingDate());
        dto.setRefType(entity.getRefType());
        dto.setClosedRemark(entity.getClosedRemark());
        dto.setSupplementary(entity.getSupplementary());
        dto.setSupplementaryFdaPoid(entity.getSupplementaryFdaPoid());
        dto.setBusinessRefBy(entity.getBusinessRefBy());
        dto.setFdaWithoutCharges(entity.getFdaWithoutCharges());
        dto.setPrintBankPoid(entity.getPrintBankPoid());
        dto.setPortCallNumber(entity.getPortCallNumber());
        dto.setNominatedPartyType(entity.getNominatedPartyType());
        dto.setNominatedPartyPoid(entity.getNominatedPartyPoid());
        dto.setDocumentSubmittedDate(entity.getDocumentSubmittedDate());
        dto.setDocumentSubmittedBy(entity.getDocumentSubmittedBy());
        dto.setDocumentSubmittedStatus(entity.getDocumentSubmittedStatus());
        dto.setFdaSubType(entity.getFdaSubType());
        dto.setSubCategory(entity.getSubCategory());
        dto.setDocumentReceivedDate(entity.getDocumentReceivedDate());
        dto.setDocumentReceivedFrom(entity.getDocumentReceivedFrom());
        dto.setDocumentReceivedStatus(entity.getDocumentReceivedStatus());
        dto.setSubmissionAcceptedDate(entity.getSubmissionAcceptedDate());
        dto.setVerificationAcceptedDate(entity.getVerificationAcceptedDate());
        dto.setSubmissionAcceptedBy(entity.getSubmissionAcceptedBy());
        dto.setVerificationAcceptedBy(entity.getVerificationAcceptedBy());
        dto.setVesselHandledBy(entity.getVesselHandledBy());
        dto.setVesselSailDate(entity.getVesselSailDate());
        dto.setAccountsVerified(entity.getAccountsVerified());
        dto.setOpsCorrectionRemarks(entity.getOpsCorrectionRemarks());
        dto.setOpsReturnedDate(entity.getOpsReturnedDate());

        return dto;
    }

    public static void mapHeaderDtoToEntity(FdaHeaderDto dto, PdaFdaHdr entity, String userId) {

        entity.setTransactionDate(dto.getTransactionDate());
        entity.setGroupPoid(dto.getGroupPoid());
        entity.setCompanyPoid(dto.getCompanyPoid());
        entity.setPrincipalPoid(dto.getPrincipalPoid());
        entity.setPrincipalContact(dto.getPrincipalContact());
        entity.setDocRef(dto.getDocRef());
        entity.setVoyagePoid(dto.getVoyagePoid());
        entity.setVesselPoid(dto.getVesselPoid());
        entity.setArrivalDate(dto.getArrivalDate());
        entity.setSailDate(dto.getSailDate());
        entity.setPortPoid(dto.getPortPoid());
        entity.setCommodityPoid(dto.getCommodityPoid());
        entity.setOperationType(dto.getOperationType());
        entity.setImportQty(dto.getImportQty());
        entity.setExportQty(dto.getExportQty());
        entity.setTotalQuantity(dto.getTotalQuantity());
        entity.setUnit(dto.getUnit());
        entity.setHarbourCallType(dto.getHarbourCallType());
        entity.setCurrencyCode(dto.getCurrencyCode());
        entity.setCurrencyRate(dto.getCurrencyRate());
        entity.setCostCentrePoid(dto.getCostCentrePoid());
        entity.setVesselVerified(dto.getVesselVerified());
        entity.setVesselVerifiedDate(dto.getVesselVerifiedDate());
        entity.setVesselVerifiedBy(dto.getVesselVerifiedBy());
        entity.setUrgentApproval(dto.getUrgentApproval());
        entity.setPrincipalAprvlDays(dto.getPrincipalAprvlDays());
        entity.setPrincipalApproved(dto.getPrincipalApproved());
        entity.setPrincipalApprovedDate(dto.getPrincipalApprovedDate());
        entity.setPrincipalApprovedBy(dto.getPrincipalApprovedBy());
        entity.setReminderMinutes(dto.getReminderMinutes());
        entity.setCargoDetails(dto.getCargoDetails());
        entity.setStatus(dto.getStatus());
        entity.setFdaClosedDate(dto.getFdaClosedDate());
        entity.setRemarks(dto.getRemarks());
        entity.setTotalAmount(dto.getTotalAmount());
        entity.setAddressPoid(dto.getAddressPoid());
        entity.setSalesmanPoid(dto.getSalesmanPoid());
        entity.setTranshipmentQty(dto.getTranshipmentQty());
        entity.setDwt(dto.getDwt());
        entity.setGrt(dto.getGrt());
        entity.setImoNumber(dto.getImoNumber());
        entity.setNrt(dto.getNrt());
        entity.setNumberOfDays(dto.getNumberOfDays());
        entity.setPortDescription(dto.getPortDescription());
        entity.setTermsPoid(dto.getTermsPoid());
        entity.setVesselTypePoid(dto.getVesselTypePoid());
        entity.setLinePoid(dto.getLinePoid());
        entity.setPrintPrincipal(dto.getPrintPrincipal());
        entity.setVoyageNo(dto.getVoyageNo());
        entity.setProfitLossAmount(dto.getProfitLossAmount());
        entity.setProfitLossPer(dto.getProfitLossPer());
        entity.setFdaClosingBy(dto.getFdaClosingBy());
        entity.setGlClosingDate(dto.getGlClosingDate());
        entity.setRefType(dto.getRefType());
        entity.setClosedRemark(dto.getClosedRemark());
        entity.setSupplementary(dto.getSupplementary());
        entity.setSupplementaryFdaPoid(dto.getSupplementaryFdaPoid());
        entity.setBusinessRefBy(dto.getBusinessRefBy());
        entity.setFdaWithoutCharges(dto.getFdaWithoutCharges());
        entity.setPrintBankPoid(dto.getPrintBankPoid());
        entity.setPortCallNumber(dto.getPortCallNumber());
        entity.setNominatedPartyType(dto.getNominatedPartyType());
        entity.setNominatedPartyPoid(dto.getNominatedPartyPoid());
        entity.setDocumentSubmittedDate(dto.getDocumentSubmittedDate());
        entity.setDocumentSubmittedBy(dto.getDocumentSubmittedBy());
        entity.setDocumentSubmittedStatus(dto.getDocumentSubmittedStatus());
        entity.setFdaSubType(dto.getFdaSubType());
        entity.setSubCategory(dto.getSubCategory());
        entity.setDocumentReceivedDate(dto.getDocumentReceivedDate());
        entity.setDocumentReceivedFrom(dto.getDocumentReceivedFrom());
        entity.setDocumentReceivedStatus(dto.getDocumentReceivedStatus());
        entity.setSubmissionAcceptedDate(dto.getSubmissionAcceptedDate());
        entity.setVerificationAcceptedDate(dto.getVerificationAcceptedDate());
        entity.setSubmissionAcceptedBy(dto.getSubmissionAcceptedBy());
        entity.setVerificationAcceptedBy(dto.getVerificationAcceptedBy());
        entity.setVesselHandledBy(dto.getVesselHandledBy());
        entity.setVesselSailDate(dto.getVesselSailDate());
        entity.setAccountsVerified(dto.getAccountsVerified());
        entity.setOpsCorrectionRemarks(dto.getOpsCorrectionRemarks());
        entity.setOpsReturnedDate(dto.getOpsReturnedDate());

        entity.setLastModifiedBy(userId);
        entity.setLastModifiedDate(LocalDateTime.now());

        if (entity.getDeleted() == null)
            entity.setDeleted("N");
    }

    public static void mapUpdateHeaderDtoToEntity(UpdateFdaHeaderRequest dto, PdaFdaHdr entity, String userId) {

        entity.setPrincipalPoid(dto.getPrincipalPoid());
        entity.setPortPoid(dto.getPortPoid());
        entity.setOperationType(dto.getOperationType());
        entity.setUnit(dto.getUnit());
        entity.setHarbourCallType(dto.getHarbourCallType());
        entity.setCargoDetails(dto.getCargoDetails());
        entity.setRemarks(dto.getRemarks());
        entity.setSalesmanPoid(dto.getSalesmanPoid());
        entity.setDwt(dto.getDwt());
        entity.setGrt(dto.getGrt());
        entity.setNrt(dto.getNrt());
        entity.setNumberOfDays(dto.getNumberOfDays());
        entity.setPortDescription(dto.getPortDescription());
        entity.setFdaSubType(dto.getFdaSubType());
        entity.setSubCategory(dto.getSubCategory());

        entity.setLastModifiedBy(userId);
        entity.setLastModifiedDate(LocalDateTime.now());

        if (entity.getDeleted() == null) {
            entity.setDeleted("N");
        }
    }
}
