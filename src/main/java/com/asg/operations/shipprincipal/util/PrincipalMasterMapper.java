package com.asg.operations.shipprincipal.util;

import com.asg.operations.shipprincipal.dto.*;
import com.asg.operations.shipprincipal.entity.AddressDetails;
import com.asg.operations.shipprincipal.entity.ShipPrincipalMaster;
import com.asg.operations.shipprincipal.entity.ShipPrincipalMasterDtl;
import com.asg.operations.shipprincipal.entity.ShipPrincipalMasterPymtDtl;
import com.asg.operations.shipprincipal.entity.ShipPrincipalPaRptDtl;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper utility for converting between Entity and DTO
 */
@Component
public class PrincipalMasterMapper {

    /**
     * Convert Entity to List DTO
     */
    public PrincipalMasterListDto toListDto(ShipPrincipalMaster entity) {
        if (entity == null) {
            return null;
        }

        return PrincipalMasterListDto.builder()
                .principalPoid(entity.getPrincipalPoid())
                .principalCode(entity.getPrincipalCode())
                .principalName(entity.getPrincipalName())
                .active(entity.getActive())
                .build();
    }

    /**
     * Convert Charge Detail Entity to DTO
     */
    public ChargeDetailDto toChargeDetailDto(ShipPrincipalMasterDtl entity) {
        if (entity == null) {
            return null;
        }

        return ChargeDetailDto.builder()
                .principalPoid(entity.getPrincipalPoid())
                .detRowId(entity.getDetRowId())
                .chargePoid(entity.getChargePoid())
                .rate(entity.getRate())
                .remarks(entity.getRemarks())
                .build();
    }

    /**
     * Convert Payment Detail Entity to DTO
     */
    public PaymentItemDTO toPaymentDetailDto(ShipPrincipalMasterPymtDtl entity) {
        if (entity == null) {
            return null;
        }

        return PaymentItemDTO.builder()
                .principalPoid(entity.getPrincipalPoid())
                .detRowId(entity.getDetRowId())
                .type(entity.getType())
                .beneficiaryName(entity.getBeneficiaryName())
                .address(entity.getAddress())
                .bank(entity.getBank())
                .bankAddress(entity.getBankAddress())
                .bankSwiftCode(entity.getBankSwiftCode())
                .swiftCode(entity.getSwiftCode())
                .accountNumber(entity.getAccountNumber())
                .iban(entity.getIban())
                .beneficiaryId(entity.getBeneficiaryId())
                .beneficiaryCountry(entity.getBeneficiaryCountry())
                .intermediaryBank(entity.getIntermediaryBank())
                .intermediaryCountryPoid(entity.getIntermediaryCountryPoid())
                .intermediaryAcct(entity.getIntermediaryAcct())
                .remarks(entity.getRemarks())
                .active(entity.getActive())
                .defaults(entity.getDefaults())
                .build();
    }

    public PrincipalMasterDto mapToDetailDTO(ShipPrincipalMaster entity) {
        PrincipalMasterDto dto = new PrincipalMasterDto();
        dto.setPrincipalPoid(entity.getPrincipalPoid());
        dto.setGroupPoid(entity.getGroupPoid());
        dto.setPrincipalCode(entity.getPrincipalCode());
        dto.setPrincipalName(entity.getPrincipalName());
        dto.setPrincipalName2(entity.getPrincipalName2());
        dto.setCountryPoid(entity.getCountryPoid());
        dto.setAddressPoid(entity.getAddressPoid());
        dto.setCreditPeriod(entity.getCreditPeriod());
        dto.setGlCodePoid(entity.getGlCodePoid());
        dto.setRemarks(entity.getRemarks());
        dto.setActive(entity.getActive());
        dto.setSeqNo(entity.getSeqno());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setLastModifiedBy(entity.getLastModifiedBy());
        dto.setLastModifiedDate(entity.getLastModifiedDate());
        dto.setGroupName(entity.getGroupName());
        dto.setGlAcctNo(entity.getGlAcctno());
        dto.setPrincipalCodeOld(entity.getPrincipalCodeOld());
        dto.setDeleted(entity.getDeleted());
        dto.setCompanyPoid(entity.getCompanyPoid());
        dto.setCurrencyCode(entity.getCurrencyCode());
        dto.setCurrencyRate(entity.getCurrencyRate());
        dto.setAgreedPeriod(entity.getAgreedPeriod());
        dto.setBuyingRate(entity.getBuyingRate());
        dto.setSellingRate(entity.getSellingRate());
        dto.setTinNumber(entity.getTinNumber());
        dto.setTaxSlab(entity.getTaxSlab());
        dto.setExemptionReason(entity.getExemptionReason());
        return dto;
    }

    public ChargeDetailDto mapToChargeDTO(ShipPrincipalMasterDtl entity) {
        ChargeDetailDto dto = new ChargeDetailDto();
        dto.setPrincipalPoid(entity.getPrincipalPoid());
        dto.setDetRowId(entity.getDetRowId());
        dto.setChargePoid(entity.getChargePoid());
        dto.setRate(entity.getRate());
        dto.setRemarks(entity.getRemarks());
        return dto;
    }

    public ChargeDetailResponseDto mapToChargeResponseDTO(ShipPrincipalMasterDtl entity) {
        return ChargeDetailResponseDto.builder()
                .principalPoid(entity.getPrincipalPoid())
                .detRowId(entity.getDetRowId())
                .chargePoid(entity.getChargePoid())
                .rate(entity.getRate())
                .remarks(entity.getRemarks())
                .build();
    }

    public PaymentItemDTO mapToPaymentDTO(ShipPrincipalMasterPymtDtl entity) {
        PaymentItemDTO dto = new PaymentItemDTO();
        dto.setPrincipalPoid(entity.getPrincipalPoid());
        dto.setDetRowId(entity.getDetRowId());
        dto.setBank(entity.getBank());
        dto.setSwiftCode(entity.getSwiftCode());
        dto.setAccountNumber(entity.getAccountNumber());
        dto.setRemarks(entity.getRemarks());
        dto.setType(entity.getType());
        dto.setBeneficiaryName(entity.getBeneficiaryName());
        dto.setAddress(entity.getAddress());
        dto.setBankAddress(entity.getBankAddress());
        dto.setBankSwiftCode(entity.getBankSwiftCode());
        dto.setIban(entity.getIban());
        dto.setIntermediaryBank(entity.getIntermediaryBank());
        dto.setBeneficiaryId(entity.getBeneficiaryId());
        dto.setIntermediaryAcct(entity.getIntermediaryAcct());
        dto.setIntermediaryOth(entity.getIntermediaryOth());
        dto.setSpecialInstruction(entity.getSpecialInstruction());
        dto.setIntermediaryCountryPoid(entity.getIntermediaryCountryPoid());
        dto.setBeneficiaryCountry(entity.getBeneficiaryCountry());
        dto.setActive(entity.getActive());
        dto.setDefaults(entity.getDefaults());
        return dto;
    }

    public void mapCreateDTOToEntity(PrincipalCreateDTO dto, ShipPrincipalMaster entity, Long groupPoid) {
        entity.setGroupPoid(groupPoid);
        entity.setPrincipalCode(dto.getPrincipalCode());
        entity.setPrincipalName(dto.getPrincipalName());
        entity.setPrincipalName2(dto.getPrincipalName2());
        entity.setCountryPoid(dto.getCountryPoid());
        entity.setCreditPeriod(dto.getCreditPeriod());
        entity.setRemarks(dto.getRemarks());
        entity.setActive("Y");
        entity.setSeqno(dto.getSeqNo());
        entity.setGroupName(dto.getGroupName());
        entity.setPrincipalCodeOld(null);
        entity.setCompanyPoid(dto.getCompanyPoid());
        entity.setCurrencyCode(dto.getCurrencyCode());
        entity.setCurrencyRate(dto.getCurrencyRate());
        entity.setAgreedPeriod(dto.getAgreedPeriod());
        entity.setBuyingRate(dto.getBuyingRate());
        entity.setSellingRate(dto.getSellingRate());
        entity.setTinNumber(dto.getTinNumber());
        entity.setTaxSlab(dto.getTaxSlab());
        entity.setExemptionReason(dto.getExemptionReason());
    }

    public void mapUpdateDTOToEntity(PrincipalUpdateDTO dto, ShipPrincipalMaster entity, Long groupPoid) {
        entity.setGroupPoid(groupPoid);
        entity.setPrincipalCode(dto.getPrincipalCode());
        entity.setPrincipalName(dto.getPrincipalName());
        entity.setPrincipalName2(dto.getPrincipalName2());
        entity.setCountryPoid(dto.getCountryPoid());
        entity.setCreditPeriod(dto.getCreditPeriod());
        entity.setRemarks(dto.getRemarks());
        entity.setActive(dto.getActive());
        entity.setSeqno(dto.getSeqNo());
        entity.setGroupName(dto.getGroupName());
        entity.setPrincipalCodeOld(dto.getPrincipalCodeOld());
        entity.setCompanyPoid(dto.getCompanyPoid());
        entity.setCurrencyCode(dto.getCurrencyCode());
        entity.setCurrencyRate(dto.getCurrencyRate());
        entity.setAgreedPeriod(dto.getAgreedPeriod());
        entity.setBuyingRate(dto.getBuyingRate());
        entity.setSellingRate(dto.getSellingRate());
        entity.setTinNumber(dto.getTinNumber());
        entity.setTaxSlab(dto.getTaxSlab());
        entity.setExemptionReason(dto.getExemptionReason());
    }

    public void mapPaymentDTOToEntity(PaymentItemDTO dto, ShipPrincipalMasterPymtDtl entity) {
        entity.setBank(dto.getBank());
        entity.setSwiftCode(dto.getSwiftCode());
        entity.setAccountNumber(dto.getAccountNumber());
        entity.setRemarks(dto.getRemarks());
        entity.setType(dto.getType());
        entity.setBeneficiaryName(dto.getBeneficiaryName());
        entity.setAddress(dto.getAddress());
        entity.setBankAddress(dto.getBankAddress());
        entity.setBankSwiftCode(dto.getBankSwiftCode());
        entity.setIban(dto.getIban());
        entity.setIntermediaryBank(dto.getIntermediaryBank());
        entity.setBeneficiaryId(dto.getBeneficiaryId());
        entity.setIntermediaryAcct(dto.getIntermediaryAcct());
        entity.setIntermediaryOth(dto.getIntermediaryOth());
        entity.setSpecialInstruction(dto.getSpecialInstruction());
        entity.setIntermediaryCountryPoid(dto.getIntermediaryCountryPoid());
        entity.setBeneficiaryCountry(dto.getBeneficiaryCountry());
        entity.setActive(dto.getActive());
        entity.setDefaults(dto.getDefaults());
    }

    public AddressDetailsDTO mapToAddressDetailDTO(AddressDetails entity) {

        String area = entity.getAreaCity();
        String city = entity.getCity();
        if ((city == null || city.isBlank()) && area != null && area.contains(",")) {
            String[] parts = area.split(",", 2);
            area = parts[0].trim();
            city = parts.length > 1 ? parts[1].trim() : null;
        }

        AddressDetailsDTO dto = new AddressDetailsDTO();
        dto.setAddressPoid(String.valueOf(entity.getAddressPoid()));
        dto.setContactPerson(entity.getContactPerson());
        dto.setDesignation(entity.getDesignation());
        dto.setOffTel1(entity.getOffTel1());
        dto.setOffTel2(entity.getOffTel2());
        dto.setMobile(entity.getMobile());
        dto.setAddressType(entity.getAddressType());

        List<String> emails = new ArrayList<>();
        if (entity.getEmail1() != null) {
            emails.add(entity.getEmail1());
        }
        if (entity.getEmail2() != null) {
            emails.add(entity.getEmail2());
        }
        dto.setEmail(emails);

        dto.setFax(entity.getFax());
        dto.setPoBox(entity.getPoBox());
        dto.setOffNo(entity.getOffNo());
        dto.setBldg(entity.getBldg());
        dto.setRoad(entity.getRoad());
        dto.setArea(area);
        dto.setCity(city);
        entity.setState(dto.getState() != null && !dto.getState().isEmpty()
                ? String.join(",", dto.getState())
                : null
        );
        dto.setLandMark(entity.getLandMark());
        dto.setWhatsappNo(entity.getWhatsappNo());
        dto.setLinkedIn(entity.getLinkedIn());
        dto.setInstagram(entity.getInstagram());
        dto.setFacebook(entity.getFacebook());
        return dto;
    }

    public ShipPrincipalPaRptDetailDto mapToPaRptDetailDTO(ShipPrincipalPaRptDtl entity) {
        ShipPrincipalPaRptDetailDto dto = new ShipPrincipalPaRptDetailDto();
        dto.setDetRowId(entity.getDetRowId());
        dto.setPortCallReportType(entity.getPortCallReportType());
        dto.setPdfTemplatePoid(entity.getPdfTemplatePoid());
        dto.setEmailTemplatePoid(entity.getEmailTemplatePoid());
        dto.setAssignedToRolePoid(entity.getAssignedToRolePoid());
        dto.setVesselType(entity.getVesselType());
        dto.setResponseTimeHrs(entity.getResponseTimeHrs());
        dto.setFrequenceHrs(entity.getFrequenceHrs());
        dto.setEscalationRole1(entity.getEscalationRole1());
        dto.setEscalationRole2(entity.getEscalationRole2());
        dto.setRemarks(entity.getRemarks());
        return dto;
    }

    public ShipPrincipalPaRptDetailResponseDto mapToPaRptDetailResponseDTO(ShipPrincipalPaRptDtl entity) {
        return ShipPrincipalPaRptDetailResponseDto.builder()
                .detRowId(entity.getDetRowId())
                .responseTimeHrs(entity.getResponseTimeHrs())
                .frequencyHrs(entity.getFrequenceHrs())
                .remarks(entity.getRemarks())
                .build();
    }

    public PaymentItemResponseDTO mapToPaymentResponseDTO(ShipPrincipalMasterPymtDtl entity) {
        return PaymentItemResponseDTO.builder()
                .principalPoid(entity.getPrincipalPoid())
                .detRowId(entity.getDetRowId())
                .beneficiaryName(entity.getBeneficiaryName())
                .address(entity.getAddress())
                .bank(entity.getBank())
                .bankAddress(entity.getBankAddress())
                .beneficiaryCountry(entity.getBeneficiaryCountry())
                .swiftCode(entity.getSwiftCode())
                .accountNumber(entity.getAccountNumber())
                .iban(entity.getIban())
                .intermediaryBank(entity.getIntermediaryBank())
                .intermediaryAcct(entity.getIntermediaryAcct())
                .bankSwiftCode(entity.getBankSwiftCode())
                .intermediaryCountryPoid(entity.getIntermediaryCountryPoid())
                .active(entity.getActive())
                .defaults(entity.getDefaults())
                .remarks(entity.getRemarks())
                .beneficiaryId(entity.getBeneficiaryId())
                .intermediaryOth(entity.getIntermediaryOth())
                .specialInstruction(entity.getSpecialInstruction())
                .build();
    }
}

