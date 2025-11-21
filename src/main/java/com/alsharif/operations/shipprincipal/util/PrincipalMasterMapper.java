package com.alsharif.operations.shipprincipal.util;


import com.alsharif.operations.shipprincipal.dto.*;
import com.alsharif.operations.shipprincipal.entity.AddressDetails;
import com.alsharif.operations.shipprincipal.entity.ShipPrincipalMaster;
import com.alsharif.operations.shipprincipal.entity.ShipPrincipalMasterDtl;
import com.alsharif.operations.shipprincipal.entity.ShipPrincipalMasterPymtDtl;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper utility for converting between Entity and DTO
 */
@Component
public class PrincipalMasterMapper {

    /**
     * Convert Entity to DTO
     */
    public PrincipalMasterDto toDto(ShipPrincipalMaster entity,
                                    List<ShipPrincipalMasterDtl> chargeDetails,
                                    List<ShipPrincipalMasterPymtDtl> paymentDetails) {
        if (entity == null) {
            return null;
        }

        PrincipalMasterDto dto = PrincipalMasterDto.builder()
                .principalPoid(entity.getPrincipalPoid())
                .principalCode(entity.getPrincipalCode())
                .principalName(entity.getPrincipalName())
                .principalName2(entity.getPrincipalName2())
                .groupPoid(entity.getGroupPoid())
                .companyPoid(entity.getCompanyPoid())
                .groupName(entity.getGroupName())
                .countryPoid(entity.getCountryPoid())
                .addressPoid(entity.getAddressPoid())
                .creditPeriod(entity.getCreditPeriod())
                .agreedPeriod(entity.getAgreedPeriod())
                .currencyCode(entity.getCurrencyCode())
                .currencyRate(entity.getCurrencyRate())
                .buyingRate(entity.getBuyingRate())
                .sellingRate(entity.getSellingRate())
                .glCodePoid(entity.getGlCodePoid())
                .glAcctNo(entity.getGlAcctno())
                .tinNumber(entity.getTinNumber())
                .taxSlab(entity.getTaxSlab())
                .exemptionReason(entity.getExemptionReason())
                .remarks(entity.getRemarks())
                .seqNo(entity.getSeqno())
                .active(entity.getActive())
                .principalCodeOld(entity.getPrincipalCodeOld())
                .deleted(entity.getDeleted())
                .createdBy(entity.getCreatedBy())
                .createdDate(entity.getCreatedDate())
                .lastModifiedBy(entity.getLastModifiedBy())
                .lastModifiedDate(entity.getLastModifiedDate())
                .build();

        // Map charge details
        if (chargeDetails != null) {
            dto.setCharges(chargeDetails.stream()
                    .map(this::toChargeDetailDto)
                    .collect(Collectors.toList()));
        }

        // Map payment details
        if (paymentDetails != null) {
            dto.setPayments(paymentDetails.stream()
                    .map(this::toPaymentDetailDto)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

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
        entity.setDetRowId(dto.getDetRowId());
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
}

