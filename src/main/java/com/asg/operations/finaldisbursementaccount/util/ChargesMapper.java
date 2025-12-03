package com.asg.operations.finaldisbursementaccount.util;

import com.asg.operations.finaldisbursementaccount.dto.FdaChargeDto;
import com.asg.operations.finaldisbursementaccount.entity.PdaFdaDtl;
import com.asg.operations.finaldisbursementaccount.key.PdaFdaDtlId;

import java.time.LocalDateTime;

public class ChargesMapper {

    public static void updateChargeEntityFromDto(FdaChargeDto dto, PdaFdaDtl entity, String userId) {
        entity.setChargePoid(dto.getChargePoid());
        entity.setCurrencyCode(dto.getCurrencyCode());
        entity.setCurrencyRate(dto.getCurrencyRate());
        entity.setDetailsFrom(dto.getDetailsFrom());
        entity.setQty(dto.getQty());
        entity.setDays(dto.getDays());
        entity.setPdaRate(dto.getPdaRate());
        entity.setRateTypePoid(dto.getRateTypePoid());
        entity.setManual(dto.getManual());
        entity.setAmount(dto.getAmount());
        entity.setRemarks(dto.getRemarks());
        entity.setRemarkQtyDays(dto.getRemarkQtyDays());
        entity.setCostAmount(dto.getCostAmount());
        entity.setFdaAmount(dto.getFdaAmount());
        entity.setSeqNo(dto.getSeqNo());
        entity.setPrincipalPoid(dto.getPrincipalPoid());
        entity.setRefDetRowId(dto.getRefDetRowId());
        entity.setRefDocId(dto.getRefDocId());
        entity.setRefDocPoid(dto.getRefDocPoid());
        entity.setBookedDocPoid(dto.getBookedDocPoid());
        entity.setDnDocId(dto.getDnDocId());
        entity.setDnDocPoid(dto.getDnDocPoid());
        entity.setFdaRemarks(dto.getPrintRemarks());
        entity.setDnFrom(dto.getDnFrom());
        entity.setCnDocId(dto.getCnDocId());
        entity.setCnDocPoid(dto.getCnDocPoid());
        entity.setDnAmount(dto.getDnAmount());
        entity.setCnAmount(dto.getCnAmount());
        entity.setCnDetRowId(dto.getCnDetRowId());
        entity.setDnDetRowId(dto.getDnDetRowId());
        entity.setTaxPoid(dto.getTaxPoid());
        entity.setTaxPercentage(dto.getTaxPercentage());
        entity.setTaxAmount(dto.getTaxAmount());
        entity.setDnTaxPoid(dto.getDnTaxPoid());
        entity.setDnTaxPercentage(dto.getDnTaxPercentage());
        entity.setDnTaxAmount(dto.getDnTaxAmount());
        entity.setDnTotalAmount(dto.getDnTotalAmount());
        entity.setCnTaxPoid(dto.getCnTaxPoid());
        entity.setCnTaxPercentage(dto.getCnTaxPercentage());
        entity.setCnTaxAmount(dto.getCnTaxAmount());
        entity.setCnTotalAmount(dto.getCnTotalAmount());
        entity.setPdaPoid(dto.getPdaPoid());
        entity.setPdaDetRowId(dto.getPdaDetRowId());
        entity.setPrintSeqNo(dto.getPrintSeqNo());

        entity.setLastModifiedBy(userId);
        entity.setLastModifiedDate(LocalDateTime.now());
    }

    public static PdaFdaDtl createNewCharge(PdaFdaDtlId id, FdaChargeDto dto, String userId) {
        PdaFdaDtl entity = new PdaFdaDtl();
        entity.setId(id);
        updateChargeEntityFromDto(dto, entity, userId);
        entity.setCreatedBy(userId);
        entity.setCreatedDate(LocalDateTime.now());
        return entity;
    }

    public static FdaChargeDto mapChargeEntityToDto(PdaFdaDtl entity) {
        FdaChargeDto dto = new FdaChargeDto();
        dto.setTransactionPoid(entity.getId().getTransactionPoid());
        dto.setDetRowId(entity.getId().getDetRowId());
        dto.setChargePoid(entity.getChargePoid());
        dto.setCurrencyCode(entity.getCurrencyCode());
        dto.setCurrencyRate(entity.getCurrencyRate());
        dto.setDetailsFrom(entity.getDetailsFrom());
        dto.setQty(entity.getQty());
        dto.setDays(entity.getDays());
        dto.setPdaRate(entity.getPdaRate());
        dto.setRateTypePoid(entity.getRateTypePoid());
        dto.setManual(entity.getManual());
        dto.setAmount(entity.getAmount());
        dto.setRemarks(entity.getRemarks());
        dto.setRemarkQtyDays(entity.getRemarkQtyDays());
        dto.setCostAmount(entity.getCostAmount());
        dto.setFdaAmount(entity.getFdaAmount());
        dto.setSeqNo(entity.getSeqNo());
        dto.setPrincipalPoid(entity.getPrincipalPoid());
        dto.setRefDetRowId(entity.getRefDetRowId());
        dto.setRefDocId(entity.getRefDocId());
        dto.setRefDocPoid(entity.getRefDocPoid());
        dto.setBookedDocPoid(entity.getBookedDocPoid());
        dto.setDnDocId(entity.getDnDocId());
        dto.setDnDocPoid(entity.getDnDocPoid());
        dto.setPrintRemarks(entity.getFdaRemarks());
        dto.setDnFrom(entity.getDnFrom());
        dto.setCnDocId(entity.getCnDocId());
        dto.setCnDocPoid(entity.getCnDocPoid());
        dto.setDnAmount(entity.getDnAmount());
        dto.setCnAmount(entity.getCnAmount());
        dto.setCnDetRowId(entity.getCnDetRowId());
        dto.setDnDetRowId(entity.getDnDetRowId());
        dto.setTaxPoid(entity.getTaxPoid());
        dto.setTaxPercentage(entity.getTaxPercentage());
        dto.setTaxAmount(entity.getTaxAmount());
        dto.setDnTaxPoid(entity.getDnTaxPoid());
        dto.setDnTaxPercentage(entity.getDnTaxPercentage());
        dto.setDnTaxAmount(entity.getDnTaxAmount());
        dto.setDnTotalAmount(entity.getDnTotalAmount());
        dto.setCnTaxPoid(entity.getCnTaxPoid());
        dto.setCnTaxPercentage(entity.getCnTaxPercentage());
        dto.setCnTaxAmount(entity.getCnTaxAmount());
        dto.setCnTotalAmount(entity.getCnTotalAmount());
        dto.setPdaPoid(entity.getPdaPoid());
        dto.setPdaDetRowId(entity.getPdaDetRowId());
        dto.setPrintSeqNo(entity.getPrintSeqNo());
        return dto;
    }

}
