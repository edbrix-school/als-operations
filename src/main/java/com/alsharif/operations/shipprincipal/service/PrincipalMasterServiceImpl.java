package com.alsharif.operations.shipprincipal.service;

import com.alsharif.operations.commonlov.repository.LovRepository;
import com.alsharif.operations.commonlov.service.LovService;
import com.alsharif.operations.exceptions.ResourceAlreadyExistsException;
import com.alsharif.operations.exceptions.ResourceNotFoundException;
import com.alsharif.operations.shipprincipal.dto.*;
import com.alsharif.operations.shipprincipal.entity.*;
import com.alsharif.operations.shipprincipal.repository.*;
import com.alsharif.operations.shipprincipal.util.PrincipalMasterMapper;
import com.alsharif.operations.user.entity.User;
import com.alsharif.operations.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrincipalMasterServiceImpl implements PrincipalMasterService {
    private final ShipPrincipalRepository principalRepository;
    private final ShipPrincipalDetailRepository chargeRepository;
    private final ShipPrincipalPaymentDetailRepository paymentRepository;
    private final AddressMasterRepository addressMasterRepository;
    private final AddressDetailsRepository addressDetailsRepository;
    private final UserRepository userRepository;
    private final GLMasterService glMasterService;
    private final AddressMasterService addressMasterService;
    private final PrincipalMasterMapper mapper;
    private final LovService lovService;


    @Override
    @Transactional
    public Page<PrincipalMasterListDto> getPrincipalList(String search, Pageable pageable) {
        Page<ShipPrincipalMaster> page = principalRepository.findAllNonDeletedWithSearch(search, pageable);
        List<PrincipalMasterListDto> dtoList = page.getContent().stream()
                .map(mapper::toListDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    @Override
    public PrincipalMasterDto getPrincipal(Long id) {
        log.debug("Fetching principal with id: {}", id);
        ShipPrincipalMaster principal = principalRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Principal not found with id: {}", id);
                    return new RuntimeException("Principal not found");
                });

        PrincipalMasterDto dto = mapper.mapToDetailDTO(principal);

        List<ShipPrincipalMasterDtl> charges = chargeRepository.findByPrincipalPoidOrderByDetRowIdAsc(id);
        dto.setCharges(charges.stream().map(mapper::mapToChargeDTO).collect(Collectors.toList()));

        List<ShipPrincipalMasterPymtDtl> payments = paymentRepository.findByPrincipalPoidOrderByDetRowIdAsc(id);
        dto.setPayments(payments.stream().map(mapper::mapToPaymentDTO).collect(Collectors.toList()));
        
        if (principal.getCountryPoid() != null) {
            dto.setCountryDet(lovService.getLovItem(principal.getCountryPoid(), "COUNTRY",
                    principal.getGroupPoid(), principal.getCompanyPoid(), null));
        }
        if (principal.getGlCodePoid() != null) {
            dto.setGlCodeDet(lovService.getLovItem(principal.getGlCodePoid(), "GL_CODE",
                    principal.getGroupPoid(), principal.getCompanyPoid(), null));
        }
        if (principal.getCompanyPoid() != null) {
            dto.setCompanyDet(lovService.getLovItem(principal.getCompanyPoid(), "COMPANY",
                    principal.getGroupPoid(), principal.getCompanyPoid(), null));
        }

        if (principal.getAddressPoid() != null) {
            List<AddressDetails> addressDetails = addressDetailsRepository.findByAddressMasterPoid(principal.getAddressPoid());
            dto.setAddressDetails(addressDetails.stream().map(mapper::mapToAddressDetailDTO).collect(Collectors.toList()));
        }

        return dto;
    }

    @Override
    @Transactional
    public PrincipalMasterDto createPrincipal(PrincipalCreateDTO dto, Long groupPoid, Long userPoid) {

        if (principalRepository.existsByPrincipalName(dto.getPrincipalName())) {
            throw new ResourceAlreadyExistsException("Principal Name already exists", "DUPLICATE_PRINCIPAL_NAME");
        }

        User user = userRepository.findByUserPoid(userPoid).orElseThrow(() -> new ResourceNotFoundException("User was not found by poid ", "user poid", userPoid));

        log.info("Creating principal with code: {}", dto.getPrincipalCode());
        Long addressPoid = null;
        if (dto.getAddressPoid() == null) {
            if (StringUtils.isBlank(dto.getAddressName())) {
                throw new RuntimeException("Address Name is required for creating new address");
            }
            boolean addressExists = addressMasterRepository.existsByAddressNameIgnoreCaseAndGroupPoid(dto.getAddressName(), groupPoid);
            if (addressExists) {
                throw new ResourceAlreadyExistsException("Address Name", dto.getAddressName());
            }
            AddressMaster newAddressMaster = new AddressMaster();
            newAddressMaster.setAddressName(dto.getAddressName());
            newAddressMaster.setGroupPoid(groupPoid);
            newAddressMaster.setSeqno(Long.valueOf(dto.getSeqNo()));
            newAddressMaster.setCreatedBy(user.getUserName());
            newAddressMaster.setCreatedDate(LocalDateTime.now());
            newAddressMaster.setLastModifiedBy(user.getUserName());
            newAddressMaster.setLastModifiedDate(LocalDateTime.now());
            addressMasterRepository.save(newAddressMaster);

            dto.setAddressPoid(newAddressMaster.getAddressMasterPoid());
            addressPoid = dto.getAddressPoid();

            if (dto.getAddressTypeMap() != null) {
                addressMasterService.saveAllDetails(dto.getAddressTypeMap(), newAddressMaster, user.getUserName());
            }
        } else {
            AddressMaster addressMaster = addressMasterRepository.findByAddressMasterPoid(dto.getAddressPoid());
            addressPoid = addressMaster.getAddressMasterPoid();
            if (dto.getAddressTypeMap() != null) {
                addressMasterService.saveAllDetails(dto.getAddressTypeMap(), addressMaster, user.getUserName());
            }
        }

        ShipPrincipalMaster principal = new ShipPrincipalMaster();
        mapper.mapCreateDTOToEntity(dto, principal, groupPoid);

        principal.setCreatedBy(user.getUserName());
        principal.setAddressPoid(addressPoid);
        principal.setCreatedDate(LocalDateTime.now());
        principal = principalRepository.save(principal);

        Long principalId = principal.getPrincipalPoid();

        if (dto.getCharges() != null) {
            for (ChargeDetailDto charge : dto.getCharges()) {
                ShipPrincipalMasterDtl entity = new ShipPrincipalMasterDtl();
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
                ShipPrincipalMasterPymtDtl entity = new ShipPrincipalMasterPymtDtl();
                entity.setPrincipalPoid(principalId);
                mapper.mapPaymentDTOToEntity(payment, entity);
                entity.setCreatedDate(LocalDateTime.now());
                paymentRepository.save(entity);
            }
        }

        if (principal.getGlCodePoid() == null) {
            CreateLedgerResponseDto result = createLedger(principal.getPrincipalPoid(), principal.getGroupPoid(), principal.getCompanyPoid(), user.getUserPoid());
            principal.setGlCodePoid(result.getGlCodePoid());
            principalRepository.save(principal);
            log.info("Successfully created GL account with POID: {} for principal: {}", result.getGlCodePoid(), principalId);
        }

        log.info("Successfully created principal with id: {}", principalId);
        return getPrincipal(principalId);
    }

    @Override
    @Transactional
    public PrincipalMasterDto updatePrincipal(Long id, PrincipalUpdateDTO dto, Long groupPoid, Long userPoid) {
        log.info("Updating principal with id: {}", id);
        ShipPrincipalMaster principal = principalRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Principal not found with id: {}", id);
                    return new RuntimeException("Principal not found");
                });

        mapper.mapUpdateDTOToEntity(dto, principal, groupPoid);

        User user = userRepository.findByUserPoid(userPoid).orElseThrow(() -> new ResourceNotFoundException("User was not found by poid ", "user poid", userPoid));
        Long addressPoid = null;
        if (dto.getAddressPoid() == null) {
            if (StringUtils.isBlank(dto.getAddressName())) {
                throw new RuntimeException("Address Name is required for creating new address");
            }
            boolean addressExists = addressMasterRepository.existsByAddressNameIgnoreCaseAndGroupPoid(dto.getAddressName(), groupPoid);
            if (addressExists) {
                throw new ResourceAlreadyExistsException("Address Name", dto.getAddressName());
            }
            AddressMaster newAddressMaster = new AddressMaster();
            newAddressMaster.setAddressName(dto.getAddressName());
            newAddressMaster.setGroupPoid(groupPoid);
            newAddressMaster.setSeqno(Long.valueOf(dto.getSeqNo()));
            newAddressMaster.setCreatedBy(user.getUserName());
            newAddressMaster.setCreatedDate(LocalDateTime.now());
            newAddressMaster.setLastModifiedBy(user.getUserName());
            newAddressMaster.setLastModifiedDate(LocalDateTime.now());
            addressMasterRepository.save(newAddressMaster);

            dto.setAddressPoid(newAddressMaster.getAddressMasterPoid());
            addressPoid = dto.getAddressPoid();

            if (dto.getAddressTypeMap() != null) {
                addressMasterService.saveAllDetails(dto.getAddressTypeMap(), newAddressMaster, user.getUserName());
            }
        } else {
            AddressMaster addressMaster = addressMasterRepository.findByAddressMasterPoid(dto.getAddressPoid());
            addressPoid = addressMaster.getAddressMasterPoid();
            if (dto.getAddressTypeMap() != null) {
                addressMasterService.saveAllDetails(dto.getAddressTypeMap(), addressMaster, user.getUserName());
            }
        }

        principal.setAddressPoid(addressPoid);
        principal.setLastModifiedBy(user.getUserName());
        principal.setLastModifiedDate(LocalDateTime.now());
        principalRepository.save(principal);

        chargeRepository.deleteByPrincipalPoid(id);

        if (dto.getCharges() != null) {
            for (ChargeDetailDto charge : dto.getCharges()) {
                ShipPrincipalMasterDtl entity = new ShipPrincipalMasterDtl();
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
                ShipPrincipalMasterPymtDtl entity = new ShipPrincipalMasterPymtDtl();
                entity.setPrincipalPoid(id);
                mapper.mapPaymentDTOToEntity(payment, entity);
                entity.setLastModifiedDate(LocalDateTime.now());
                paymentRepository.save(entity);
            }
        }

        if (principal.getGlCodePoid() == null) {
            log.debug("Creating GL account for principal: {}", id);
            CreateLedgerResponseDto result = createLedger(principal.getPrincipalPoid(), principal.getGroupPoid(), principal.getCompanyPoid(), user.getUserPoid());
            principal.setGlCodePoid(result.getGlCodePoid());
            principalRepository.save(principal);
            log.info("Successfully created GL account with POID: {} for principal: {}", result.getGlCodePoid(), principal.getPrincipalPoid());
        }
        log.info("Successfully updated principal with id: {}", id);
        return getPrincipal(id);
    }

    @Override
    @Transactional
    public void toggleActive(Long id) {
        log.info("Toggling active status for principal with id: {}", id);
        ShipPrincipalMaster principal = principalRepository.findById(id)
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

    @Override
    @Transactional
    public void deletePrincipal(Long id) {
        log.info("Soft deleting principal with id: {}", id);

        ShipPrincipalMaster principal = principalRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Principal not found with id", "id", id));
        principal.setActive("N");
        principal.setLastModifiedDate(LocalDateTime.now());
        principalRepository.save(principal);
        log.info("Successfully soft deleted principal with id: {}", id);
    }


    @Override
    @Transactional
    public CreateLedgerResponseDto createLedger(Long principalPoid,
                                                Long groupPoid,
                                                Long companyPoid,
                                                Long userPoid) {
        // Get principal
        ShipPrincipalMaster principal = principalRepository.findByIdAndNotDeleted(principalPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Principal not found", "principalPoid", principalPoid));

        User user = userRepository.findByUserPoid(userPoid).orElseThrow(() -> new ResourceNotFoundException("User was not found by poid ", "user poid", userPoid));

        // Check if GL ledger already exists
        if (principal.getGlCodePoid() != null) {
            return CreateLedgerResponseDto.builder()
                    .success(false)
                    .message("GL ledger already created for the Principal")
                    .errorCode("GL_ALREADY_EXISTS")
                    .build();
        }

        // Check if PrincipalCode exists
        if (principal.getPrincipalCode() == null || principal.getPrincipalCode().isEmpty()) {
            return CreateLedgerResponseDto.builder()
                    .success(false)
                    .message("PrincipalCode is not present, Please save the Document and proceed")
                    .errorCode("PRINCIPAL_CODE_MISSING")
                    .build();
        }

        // Use principal's company/group if not provided
        if (companyPoid == null) {
            companyPoid = principal.getCompanyPoid();
        }
        if (groupPoid == null) {
            groupPoid = principal.getGroupPoid();
        }

        // Call stored procedure
        CreateLedgerResult result = glMasterService.createGlMaster(
                groupPoid,
                companyPoid,
                principalPoid,
                user.getUserName()
        );

        if (result.isError()) {
            return CreateLedgerResponseDto.builder()
                    .success(false)
                    .message("Some error occurred while creating GL: " + result.getResult())
                    .errorCode("GL_CREATE_ERROR")
                    .build();
        }

        if (result.isSuccess()) {
            // Update principal with GL Code
            principal.setGlCodePoid(result.getNewGlPoid());
            principal.setGlAcctno(result.getGlAcctno());
            principal.setLastModifiedBy(user.getUserName());
            principalRepository.save(principal);

            return CreateLedgerResponseDto.builder()
                    .success(true)
                    .message(result.getResult())
                    .glCodePoid(result.getNewGlPoid())
                    .glAcctno(result.getGlAcctno())
                    .build();
        }

        return CreateLedgerResponseDto.builder()
                .success(false)
                .message("Unknown error occurred")
                .errorCode("UNKNOWN_ERROR")
                .build();
    }
}
