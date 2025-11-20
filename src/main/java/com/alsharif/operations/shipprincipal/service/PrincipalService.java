package com.alsharif.operations.shipprincipal.service;

import com.alsharif.operations.shipprincipal.dto.*;
import com.alsharif.operations.shipprincipal.entity.*;
import com.alsharif.operations.shipprincipal.repository.*;
import com.alsharif.operations.user.entity.User;
import com.alsharif.operations.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrincipalService {
    private final ShipPrincipalRepository principalRepository;
    private final ShipPrincipalDetailRepository chargeRepository;
    private final ShipPrincipalPaymentDetailRepository paymentRepository;
    private final AddressMasterRepository addressMasterRepository;
    private final AddressDetailsRepository addressDetailsRepository;
    private final UserRepository userRepository;
    private final DataSource dataSource;

    public PrincipalDetailDTO getPrincipal(Long id) {
        log.debug("Fetching principal with id: {}", id);
        ShipPrincipalEntity principal = principalRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Principal not found with id: {}", id);
                    return new RuntimeException("Principal not found");
                });
        
        PrincipalDetailDTO dto = mapToDetailDTO(principal);
        
        List<ShipPrincipalDetailEntity> charges = chargeRepository.findByPrincipalPoidOrderByDetRowIdAsc(id);
        dto.setCharges(charges.stream().map(this::mapToChargeDTO).collect(Collectors.toList()));
        
        List<ShipPrincipalPaymentDetailEntity> payments = paymentRepository.findByPrincipalPoidOrderByDetRowIdAsc(id);
        dto.setPayments(payments.stream().map(this::mapToPaymentDTO).collect(Collectors.toList()));
        
        if (principal.getAddressPoid() != null) {
            List<AddressDetails> addressDetails = addressDetailsRepository.findByAddressMasterPoid(principal.getAddressPoid());
            dto.setAddressDetails(addressDetails.stream().map(this::mapToAddressDetailDTO).collect(Collectors.toList()));
        }
        
        return dto;
    }

    @Transactional
    public PrincipalDetailDTO createPrincipal(PrincipalCreateDTO dto, Long userPoid) {
        log.info("Creating principal with code: {}", dto.getPrincipalCode());
        Long addressPoid = null;
        if (dto.getAddress() != null) {
            log.debug("Creating address for principal: {}", dto.getPrincipalCode());
            addressPoid = createAddress(dto.getAddress(), userPoid);
        }
        
        ShipPrincipalEntity principal = new ShipPrincipalEntity();
        mapCreateDTOToEntity(dto, principal);
        User user = userRepository.findByUserPoid(userPoid).orElseThrow(() -> new RuntimeException("User was not found by poid " + userPoid));
        principal.setCreatedBy(user.getUserName());
        principal.setAddressPoid(addressPoid);
        principal.setCreatedDate(LocalDateTime.now());
        principal = principalRepository.save(principal);
        
        Long principalId = principal.getPrincipalPoid();
        
        if (dto.getCharges() != null) {
            for (ChargeItemDTO charge : dto.getCharges()) {
                ShipPrincipalDetailEntity entity = new ShipPrincipalDetailEntity();
                entity.setPrincipalPoid(principalId);
                entity.setDetRowId(charge.getDetRowId());
                entity.setChargePoid(charge.getChargePoid());
                entity.setRate(charge.getRate());
                entity.setRemarks(charge.getRemarks());
                entity.setCreatedDate(LocalDateTime.now());
                chargeRepository.save(entity);
            }
        }
        
        if (dto.getPayments() != null) {
            for (PaymentItemDTO payment : dto.getPayments()) {
                ShipPrincipalPaymentDetailEntity entity = new ShipPrincipalPaymentDetailEntity();
                entity.setPrincipalPoid(principalId);
                mapPaymentDTOToEntity(payment, entity);
                entity.setCreatedDate(LocalDateTime.now());
                paymentRepository.save(entity);
            }
        }
        
        if (principal.getGlCodePoid() == null) {
            log.debug("Creating GL account for principal: {}", principalId);
            callGLCreateProcedure(principalId);
        }
        
        log.info("Successfully created principal with id: {}", principalId);
        return getPrincipal(principalId);
    }

    @Transactional
    public PrincipalDetailDTO updatePrincipal(Long id, PrincipalUpdateDTO dto) {
        log.info("Updating principal with id: {}", id);
        ShipPrincipalEntity principal = principalRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Principal not found with id: {}", id);
                    return new RuntimeException("Principal not found");
                });
        
        mapUpdateDTOToEntity(dto, principal);
        principal.setLastModifiedDate(LocalDateTime.now());
        principalRepository.save(principal);
        
        chargeRepository.deleteByPrincipalPoid(id);
        
        if (dto.getCharges() != null) {
            for (ChargeItemDTO charge : dto.getCharges()) {
                ShipPrincipalDetailEntity entity = new ShipPrincipalDetailEntity();
                entity.setPrincipalPoid(id);
                entity.setDetRowId(charge.getDetRowId());
                entity.setChargePoid(charge.getChargePoid());
                entity.setRate(charge.getRate());
                entity.setRemarks(charge.getRemarks());
                entity.setLastModifiedDate(LocalDateTime.now());
                chargeRepository.save(entity);
            }
        }
        
        paymentRepository.deleteByPrincipalPoid(id);
        
        if (dto.getPayments() != null) {
            for (PaymentItemDTO payment : dto.getPayments()) {
                ShipPrincipalPaymentDetailEntity entity = new ShipPrincipalPaymentDetailEntity();
                entity.setPrincipalPoid(id);
                mapPaymentDTOToEntity(payment, entity);
                entity.setLastModifiedDate(LocalDateTime.now());
                paymentRepository.save(entity);
            }
        }
        
        if (principal.getGlCodePoid() == null) {
            log.debug("Creating GL account for principal: {}", id);
            callGLCreateProcedure(id);
        }
        log.info("Successfully updated principal with id: {}", id);
        return getPrincipal(id);
    }

    @Transactional
    public void toggleActive(Long id) {
        log.info("Toggling active status for principal with id: {}", id);
        ShipPrincipalEntity principal = principalRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Principal not found with id: {}", id);
                    return new RuntimeException("Principal not found");
                });
        String newStatus = "Y".equals(principal.getActive()) ? "N" : "Y";
        principal.setActive(newStatus);
        principal.setLastModifiedDate(LocalDateTime.now());
        principalRepository.save(principal);
        log.info("Successfully toggled active status to {} for principal with id: {}", newStatus, id);
    }

    @Transactional
    public void deletePrincipal(Long id) {
        log.info("Soft deleting principal with id: {}", id);
        ShipPrincipalEntity principal = principalRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Principal not found with id: {}", id);
                    return new RuntimeException("Principal not found");
                });
        principal.setActive("N");
        principal.setLastModifiedDate(LocalDateTime.now());
        principalRepository.save(principal);
        log.info("Successfully soft deleted principal with id: {}", id);
    }

    private void callGLCreateProcedure(Long principalId) {
        log.debug("Calling GL create procedure for principal: {}", principalId);
        ShipPrincipalEntity principal = principalRepository.findById(principalId).orElseThrow();
        
        String sql = "BEGIN PROC_GL_MASTER_CREATE(?,?,?,?,?,?,?,?); END;";
        
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setBigDecimal(1, BigDecimal.valueOf(principal.getGroupPoid()));
            stmt.setBigDecimal(2, BigDecimal.valueOf(principal.getCompanyPoid()));
            stmt.setString(3, principal.getCreatedBy());
            stmt.setString(4, principal.getPrincipalCode());
            stmt.setString(5, principal.getPrincipalName());
            stmt.setString(6, "TRADE_PRINCIPAL");
            stmt.registerOutParameter(7, Types.VARCHAR);
            stmt.registerOutParameter(8, Types.NUMERIC);
            
            stmt.execute();
            
            String status = stmt.getString(7);
            BigDecimal newGlPoid = stmt.getBigDecimal(8);
            
            if (status != null && status.startsWith("SUCCESS") && newGlPoid != null) {
                principal.setGlCodePoid(newGlPoid.longValue());
                principalRepository.save(principal);
                log.info("Successfully created GL account with POID: {} for principal: {}", newGlPoid, principalId);
            } else {
                log.error("GL Creation failed for principal: {} with status: {}", principalId, status);
                throw new RuntimeException("GL Creation failed: " + status);
            }
        } catch (Exception e) {
            log.error("Error calling GL procedure for principal: {}", principalId, e);
            throw new RuntimeException("Error calling GL procedure: " + e.getMessage(), e);
        }
    }

    private PrincipalDetailDTO mapToDetailDTO(ShipPrincipalEntity entity) {
        PrincipalDetailDTO dto = new PrincipalDetailDTO();
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
        dto.setSeqNo(entity.getSeqNo());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setLastModifiedBy(entity.getLastModifiedBy());
        dto.setLastModifiedDate(entity.getLastModifiedDate());
        dto.setGroupName(entity.getGroupName());
        dto.setGlAcctNo(entity.getGlAcctNo());
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

    private ChargeItemDTO mapToChargeDTO(ShipPrincipalDetailEntity entity) {
        ChargeItemDTO dto = new ChargeItemDTO();
        dto.setDetRowId(entity.getDetRowId());
        dto.setChargePoid(entity.getChargePoid());
        dto.setRate(entity.getRate());
        dto.setRemarks(entity.getRemarks());
        return dto;
    }

    private PaymentItemDTO mapToPaymentDTO(ShipPrincipalPaymentDetailEntity entity) {
        PaymentItemDTO dto = new PaymentItemDTO();
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
        dto.setActive(entity.getActive().equals("Y"));
        dto.setDefaults(entity.getDefaults().equals("y"));
        return dto;
    }

    private Long createAddress(AddressMasterDTO dto, Long userPoid) {
        AddressMaster addressMaster = new AddressMaster();
        addressMaster.setGroupPoid(dto.getGroupPoid());
        String id = UUID.randomUUID().toString().substring(0, 5);
        addressMaster.setAddressName(id);
        addressMaster.setActive("Y");
        addressMaster.setCreatedDate(LocalDateTime.now());
        addressMaster = addressMasterRepository.save(addressMaster);
        
        Long addressMasterPoid = addressMaster.getAddressMasterPoid();
        log.info("Successfully created address master with POID: {}", addressMasterPoid);
        
        if (dto.getDetails() != null) {
            Long addressPoid = System.currentTimeMillis();
            for (AddressDetailDTO detail : dto.getDetails()) {
                AddressDetails addressDetail = new AddressDetails();
                addressDetail.setAddressPoid(addressPoid++);
                addressDetail.setAddressMasterPoid(addressMasterPoid);
                addressDetail.setContactPerson(detail.getContactPerson());
                addressDetail.setDesignation(detail.getDesignation());
                addressDetail.setOffTel1(detail.getOffTel1());
                addressDetail.setOffTel2(detail.getOffTel2());
                addressDetail.setMobile(detail.getMobile());
                addressDetail.setEmail1(detail.getEmail1());
                addressDetail.setEmail2(detail.getEmail2());
                addressDetail.setFax(detail.getFax());
                addressDetail.setPoBox(detail.getPoBox());
                addressDetail.setOffNo(detail.getOffNo());
                addressDetail.setBldg(detail.getBldg());
                addressDetail.setRoad(detail.getRoad());
                addressDetail.setAreaCity(detail.getAreaCity());
                addressDetail.setState(detail.getState());
                addressDetail.setLandMark(detail.getLandMark());
                addressDetail.setWhatsappNo(detail.getWhatsappNo());
                addressDetail.setLinkedIn(detail.getLinkedIn());
                addressDetail.setInstagram(detail.getInstagram());
                addressDetail.setFacebook(detail.getFacebook());
                addressDetail.setCreatedDate(LocalDateTime.now());
                addressDetail.setCreatedBy(userPoid.toString());
                addressDetailsRepository.save(addressDetail);
            }
            log.debug("Created {} address details for address master: {}", dto.getDetails().size(), addressMasterPoid);
        }
        
        return addressMasterPoid;
    }
    
    private void mapCreateDTOToEntity(PrincipalCreateDTO dto, ShipPrincipalEntity entity) {
        entity.setGroupPoid(dto.getGroupPoid());
        entity.setPrincipalCode(dto.getPrincipalCode());
        entity.setPrincipalName(dto.getPrincipalName());
        entity.setPrincipalName2(dto.getPrincipalName2());
        entity.setCountryPoid(dto.getCountryPoid());
        entity.setCreditPeriod(dto.getCreditPeriod());
        entity.setRemarks(dto.getRemarks());
        entity.setActive(dto.isActive() ? "Y" : "F");
        entity.setSeqNo(dto.getSeqNo());
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

    private void mapUpdateDTOToEntity(PrincipalUpdateDTO dto, ShipPrincipalEntity entity) {
        entity.setGroupPoid(dto.getGroupPoid());
        entity.setPrincipalCode(dto.getPrincipalCode());
        entity.setPrincipalName(dto.getPrincipalName());
        entity.setPrincipalName2(dto.getPrincipalName2());
        entity.setCountryPoid(dto.getCountryPoid());
        entity.setCreditPeriod(dto.getCreditPeriod());
        entity.setRemarks(dto.getRemarks());
        entity.setActive(dto.isActive() ? "Y" : "N");
        entity.setSeqNo(dto.getSeqNo());
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

    private void mapPaymentDTOToEntity(PaymentItemDTO dto, ShipPrincipalPaymentDetailEntity entity) {
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
        entity.setActive(dto.isActive() ? "Y" : "N");
        entity.setDefaults(dto.isDefaults() ? "Y" : "N");
    }
    
    private AddressDetailDTO mapToAddressDetailDTO(AddressDetails entity) {
        AddressDetailDTO dto = new AddressDetailDTO();
        dto.setContactPerson(entity.getContactPerson());
        dto.setDesignation(entity.getDesignation());
        dto.setOffTel1(entity.getOffTel1());
        dto.setOffTel2(entity.getOffTel2());
        dto.setMobile(entity.getMobile());
        dto.setEmail1(entity.getEmail1());
        dto.setEmail2(entity.getEmail2());
        dto.setFax(entity.getFax());
        dto.setPoBox(entity.getPoBox());
        dto.setOffNo(entity.getOffNo());
        dto.setBldg(entity.getBldg());
        dto.setRoad(entity.getRoad());
        dto.setAreaCity(entity.getAreaCity());
        dto.setState(entity.getState());
        dto.setLandMark(entity.getLandMark());
        dto.setWhatsappNo(entity.getWhatsappNo());
        dto.setLinkedIn(entity.getLinkedIn());
        dto.setInstagram(entity.getInstagram());
        dto.setFacebook(entity.getFacebook());
        return dto;
    }
}
