package com.asg.operations.finaldisbursementaccount.util;

import com.asg.operations.finaldisbursementaccount.dto.FdaChargeDto;
import com.asg.operations.finaldisbursementaccount.entity.PdaFdaDtl;
import com.asg.operations.finaldisbursementaccount.key.PdaFdaDtlId;

public class ChargesMapper {

    public static void updateChargeEntityFromDto(FdaChargeDto dto, PdaFdaDtl entity, String userId) {
//        entity.setChargePoid(dto.getChargePoid());

//        entity.setDetailsFrom(dto.getDetailsFrom());
//        entity.setQty(dto.getQty());
//        entity.setDays(dto.getDays());
//        entity.setPdaRate(dto.getPdaRate());
        entity.setCurrencyCode(dto.getCurrencyCode());
        entity.setCurrencyRate(dto.getCurrencyRate());
//        entity.setAmount(dto.getAmount());
//        entity.setRemarks(dto.getRemarks());
//        entity.setRemarkQtyDays(dto.getRemarkQtyDays());
//        entity.setCostAmount(dto.getCostAmount());
//        entity.setFdaAmount(dto.getFdaAmount());
//        entity.setSeqNo(dto.getSeqNo());
        entity.setPrincipalPoid(dto.getPrincipalPoid());
        entity.setFdaRemarks(dto.getPrintRemarks());
//        entity.setDnAmount(dto.getDnAmount());
//        entity.setCnAmount(dto.getCnAmount());
//        entity.setDnTaxAmount(dto.getDnTaxAmount());
//        entity.setDnTotalAmount(dto.getDnTotalAmount());
//        entity.setCnTaxAmount(dto.getCnTaxAmount());
        entity.setPrintSeqNo(dto.getPrintSeqNo());
    }

    public static PdaFdaDtl createNewCharge(PdaFdaDtlId id, FdaChargeDto dto, String userId) {
        PdaFdaDtl entity = new PdaFdaDtl();
        entity.setId(id);
        updateChargeEntityFromDto(dto, entity, userId);
        return entity;
    }

    public static FdaChargeDto mapChargeEntityToDto(PdaFdaDtl entity) {
        FdaChargeDto dto = new FdaChargeDto();
        dto.setTransactionPoid(entity.getId().getTransactionPoid());
        dto.setDetRowId(entity.getId().getDetRowId());
        dto.setChargePoid(entity.getChargePoid());
        dto.setDetailsFrom(entity.getDetailsFrom());
        dto.setQty(entity.getQty());
        dto.setDays(entity.getDays());
        dto.setPdaRate(entity.getPdaRate());
        dto.setCurrencyCode(entity.getCurrencyCode());
        dto.setCurrencyRate(entity.getCurrencyRate());
        dto.setAmount(entity.getAmount());
        dto.setRemarks(entity.getRemarks());
        dto.setRemarkQtyDays(entity.getRemarkQtyDays());
        dto.setCostAmount(entity.getCostAmount());
        dto.setFdaAmount(entity.getFdaAmount());
        dto.setSeqNo(entity.getSeqNo());
        dto.setPrincipalPoid(entity.getPrincipalPoid());
        dto.setPrintRemarks(entity.getFdaRemarks());
        dto.setDnAmount(entity.getDnAmount());
        dto.setCnAmount(entity.getCnAmount());
        dto.setDnTaxAmount(entity.getDnTaxAmount());
        dto.setDnTotalAmount(entity.getDnTotalAmount());
        dto.setCnTaxAmount(entity.getCnTaxAmount());
        dto.setPrintSeqNo(entity.getPrintSeqNo());
        dto.setLastModifiedBy(entity.getLastModifiedBy());
        dto.setLastModifiedDate(entity.getLastModifiedDate());
        return dto;
    }

}
