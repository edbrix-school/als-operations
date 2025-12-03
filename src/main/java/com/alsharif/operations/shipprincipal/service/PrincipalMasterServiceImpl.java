package com.alsharif.operations.shipprincipal.service;

import com.alsharif.operations.commonlov.repository.LovRepository;
import com.alsharif.operations.commonlov.service.LovService;
import com.alsharif.operations.crew.dto.ValidationError;
import com.alsharif.operations.exceptions.ResourceAlreadyExistsException;
import com.alsharif.operations.exceptions.ResourceNotFoundException;
import com.alsharif.operations.exceptions.ValidationException;
import com.alsharif.operations.shipprincipal.dto.*;
import com.alsharif.operations.shipprincipal.dto.ShipPrincipalPaRptDetailResponseDto;
import com.alsharif.operations.shipprincipal.entity.*;
import com.alsharif.operations.commonlov.dto.LovItem;
import com.alsharif.operations.commonlov.dto.LovResponse;
import com.alsharif.operations.commonlov.service.LovService;
import com.alsharif.operations.shipprincipal.repository.*;
import com.alsharif.operations.shipprincipal.util.PrincipalMasterMapper;
import com.alsharif.operations.portcallreport.enums.ActionType;
import com.alsharif.operations.vesseltype.entity.VesselType;
import com.alsharif.operations.vesseltype.repository.VesselTypeRepository;
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
    private final ShipPrincipalPaRptDtlRepository paRptDtlRepository;
    private final AddressMasterRepository addressMasterRepository;
    private final AddressDetailsRepository addressDetailsRepository;
    private final UserRepository userRepository;
    private final GLMasterService glMasterService;
    private final AddressMasterService addressMasterService;
    private final PrincipalMasterMapper mapper;
    private final LovService lovService;
    private final VesselTypeRepository vesselTypeRepository;


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

        List<ShipPrincipalMasterDtl> charges = chargeRepository.findByPrincipalPoidOrderByDetRowIdAsc(id);
        dto.setCharges(mapChargesWithLov(charges));

        List<ShipPrincipalMasterPymtDtl> payments = paymentRepository.findByPrincipalPoidOrderByDetRowIdAsc(id);
        dto.setPayments(mapPaymentsWithLov(payments));

        List<ShipPrincipalPaRptDtl> paRptDetails = paRptDtlRepository.findByPrincipalPoidOrderByDetRowIdAsc(id);
        dto.setPortActivityReportDetails(mapPaRptDetailsWithLov(paRptDetails));

        if (principal.getAddressPoid() != null) {
            List<AddressDetails> addressDetails = addressDetailsRepository.findByAddressMasterPoid(principal.getAddressPoid());
            dto.setAddressDetails(addressDetails.stream().map(mapper::mapToAddressDetailDTO).collect(Collectors.toList()));
        }

        return dto;
    }

    @Override
    @Transactional
    public PrincipalMasterDto createPrincipal(PrincipalCreateDTO dto, Long groupPoid, Long userPoid) {
        log.info("Creating principal with name: {}", dto.getPrincipalName());

        if (principalRepository.existsByPrincipalName(dto.getPrincipalName())) {
            log.error("Principal name already exists: {}", dto.getPrincipalName());
            throw new ResourceAlreadyExistsException("Principal Name already exists", "DUPLICATE_PRINCIPAL_NAME");
        }

        User user = userRepository.findByUserPoid(userPoid).orElseThrow(() -> new ResourceNotFoundException("User was not found by poid ", "user poid", userPoid));

        log.debug("Creating principal with code: {}", dto.getPrincipalCode());
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
            Long nextDetRowId = chargeRepository.findMaxDetRowIdByPrincipalPoid(principalId) + 1;
            for (ChargeDetailDto charge : dto.getCharges()) {
                ShipPrincipalMasterDtl entity = new ShipPrincipalMasterDtl();
                entity.setPrincipalPoid(principalId);
                entity.setDetRowId(nextDetRowId++);
                entity.setChargePoid(charge.getChargePoid());
                entity.setRate(charge.getRate());
                entity.setRemarks(charge.getRemarks());
                entity.setCreatedDate(LocalDateTime.now());
                chargeRepository.save(entity);
            }
        }

        if (dto.getPayments() != null) {
            Long nextDetRowId = paymentRepository.findMaxDetRowIdByPrincipalPoid(principalId) + 1;
            for (PaymentItemDTO payment : dto.getPayments()) {
                ShipPrincipalMasterPymtDtl entity = new ShipPrincipalMasterPymtDtl();
                entity.setPrincipalPoid(principalId);
                entity.setDetRowId(nextDetRowId++);
                mapper.mapPaymentDTOToEntity(payment, entity);
                entity.setCreatedDate(LocalDateTime.now());
                paymentRepository.save(entity);
            }
        }

        if (dto.getPortActivityReportDetails() != null) {
            log.debug("Processing {} port activity report details", dto.getPortActivityReportDetails().size());
            List<Long> validVesselTypePoids = vesselTypeRepository.findAllActive().stream()
                    .map(VesselType::getVesselTypePoid)
                    .toList();
            
            Long nextDetRowId = paRptDtlRepository.findMaxDetRowIdByPrincipalPoid(principalId) + 1;
            int index = 0;
            for (ShipPrincipalPaRptDetailDto paRptDetail : dto.getPortActivityReportDetails()) {
                if (paRptDetail.getVesselType() != null && !validVesselTypePoids.contains(Long.parseLong(paRptDetail.getVesselType()))) {
                    log.error("Invalid vessel type POID: {}", paRptDetail.getVesselType());
                    throw new ValidationException("Invalid vessel type", List.of(new ValidationError(index, "vesselType", "Invalid vessel type POID: " + paRptDetail.getVesselType())));
                }
                index++;
                
                ShipPrincipalPaRptDtl entity = new ShipPrincipalPaRptDtl();
                entity.setPrincipalPoid(principalId);
                entity.setDetRowId(nextDetRowId++);
                entity.setPortCallReportType(paRptDetail.getPortCallReportType());
                entity.setPdfTemplatePoid(paRptDetail.getPdfTemplatePoid());
                entity.setEmailTemplatePoid(paRptDetail.getEmailTemplatePoid());
                entity.setAssignedToRolePoid(paRptDetail.getAssignedToRolePoid());
                entity.setVesselType(paRptDetail.getVesselType());
                entity.setResponseTimeHrs(paRptDetail.getResponseTimeHrs());
                entity.setFrequenceHrs(paRptDetail.getFrequenceHrs());
                entity.setEscalationRole1(paRptDetail.getEscalationRole1());
                entity.setEscalationRole2(paRptDetail.getEscalationRole2());
                entity.setRemarks(paRptDetail.getRemarks());
                entity.setCreatedBy(user.getUserName());
                entity.setCreatedDate(LocalDateTime.now());
                paRptDtlRepository.save(entity);
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

        if (dto.getCharges() != null) {
            for (ChargeDetailDto charge : dto.getCharges()) {
                ActionType action = charge.getActionType();

                if (action == ActionType.isCreated) {
                    Long nextDetRowId = chargeRepository.findMaxDetRowIdByPrincipalPoid(id) + 1;
                    ShipPrincipalMasterDtl entity = new ShipPrincipalMasterDtl();
                    entity.setPrincipalPoid(id);
                    entity.setDetRowId(nextDetRowId);
                    entity.setChargePoid(charge.getChargePoid());
                    entity.setRate(charge.getRate());
                    entity.setRemarks(charge.getRemarks());
                    entity.setCreatedDate(LocalDateTime.now());
                    chargeRepository.save(entity);
                } else if (action == ActionType.isUpdated) {
                    chargeRepository.findById(new ShipPrincipalMasterDtlId(id, charge.getDetRowId()))
                            .ifPresent(existing -> {
                                existing.setChargePoid(charge.getChargePoid());
                                existing.setRate(charge.getRate());
                                existing.setRemarks(charge.getRemarks());
                                existing.setLastModifiedDate(LocalDateTime.now());
                                chargeRepository.save(existing);
                            });
                } else if (action == ActionType.isDeleted) {
                    chargeRepository.deleteById(new ShipPrincipalMasterDtlId(id, charge.getDetRowId()));
                }
            }
        }

        if (dto.getPayments() != null) {
            for (PaymentItemDTO payment : dto.getPayments()) {
                ActionType action = payment.getActionType();

                if (action == ActionType.isCreated) {
                    Long nextDetRowId = paymentRepository.findMaxDetRowIdByPrincipalPoid(id) + 1;
                    ShipPrincipalMasterPymtDtl entity = new ShipPrincipalMasterPymtDtl();
                    entity.setPrincipalPoid(id);
                    entity.setDetRowId(nextDetRowId);
                    mapper.mapPaymentDTOToEntity(payment, entity);
                    entity.setCreatedDate(LocalDateTime.now());
                    paymentRepository.save(entity);
                } else if (action == ActionType.isUpdated) {
                    paymentRepository.findById(new ShipPrincipalMasterDtlId(id, payment.getDetRowId()))
                            .ifPresent(existing -> {
                                mapper.mapPaymentDTOToEntity(payment, existing);
                                existing.setLastModifiedDate(LocalDateTime.now());
                                paymentRepository.save(existing);
                            });
                } else if (action == ActionType.isDeleted) {
                    paymentRepository.deleteById(new ShipPrincipalMasterDtlId(id, payment.getDetRowId()));
                }
            }
        }

        if (dto.getPortActivityReportDetails() != null) {
            log.debug("Updating {} port activity report details", dto.getPortActivityReportDetails().size());
            List<Long> validVesselTypePoids = vesselTypeRepository.findAllActive().stream()
                    .map(VesselType::getVesselTypePoid)
                    .toList();
            
            int index = 0;
            for (ShipPrincipalPaRptDetailDto paRptDetail : dto.getPortActivityReportDetails()) {
                if (paRptDetail.getVesselType() != null && !validVesselTypePoids.contains(Long.parseLong(paRptDetail.getVesselType()))) {
                    log.error("Invalid vessel type POID: {}", paRptDetail.getVesselType());
                    throw new ValidationException("Invalid vessel type", List.of(new ValidationError(index, "vesselType", "Invalid vessel type POID: " + paRptDetail.getVesselType())));
                }
                index++;
                
                ActionType action = paRptDetail.getActionType();

                if (action == ActionType.isCreated) {
                    Long nextDetRowId = paRptDtlRepository.findMaxDetRowIdByPrincipalPoid(id) + 1;
                    ShipPrincipalPaRptDtl entity = new ShipPrincipalPaRptDtl();
                    entity.setPrincipalPoid(id);
                    entity.setDetRowId(nextDetRowId);
                    entity.setPortCallReportType(paRptDetail.getPortCallReportType());
                    entity.setPdfTemplatePoid(paRptDetail.getPdfTemplatePoid());
                    entity.setEmailTemplatePoid(paRptDetail.getEmailTemplatePoid());
                    entity.setAssignedToRolePoid(paRptDetail.getAssignedToRolePoid());
                    entity.setVesselType(paRptDetail.getVesselType());
                    entity.setResponseTimeHrs(paRptDetail.getResponseTimeHrs());
                    entity.setFrequenceHrs(paRptDetail.getFrequenceHrs());
                    entity.setEscalationRole1(paRptDetail.getEscalationRole1());
                    entity.setEscalationRole2(paRptDetail.getEscalationRole2());
                    entity.setRemarks(paRptDetail.getRemarks());
                    entity.setCreatedBy(user.getUserName());
                    entity.setCreatedDate(LocalDateTime.now());
                    paRptDtlRepository.save(entity);
                } else if (action == ActionType.isUpdated) {
                    paRptDtlRepository.findById(new ShipPrincipalPaRptDtlId(id, paRptDetail.getDetRowId()))
                            .ifPresent(existing -> {
                                existing.setPortCallReportType(paRptDetail.getPortCallReportType());
                                existing.setPdfTemplatePoid(paRptDetail.getPdfTemplatePoid());
                                existing.setEmailTemplatePoid(paRptDetail.getEmailTemplatePoid());
                                existing.setAssignedToRolePoid(paRptDetail.getAssignedToRolePoid());
                                existing.setVesselType(paRptDetail.getVesselType());
                                existing.setResponseTimeHrs(paRptDetail.getResponseTimeHrs());
                                existing.setFrequenceHrs(paRptDetail.getFrequenceHrs());
                                existing.setEscalationRole1(paRptDetail.getEscalationRole1());
                                existing.setEscalationRole2(paRptDetail.getEscalationRole2());
                                existing.setRemarks(paRptDetail.getRemarks());
                                existing.setLastModifiedBy(user.getUserName());
                                existing.setLastModifiedDate(LocalDateTime.now());
                                paRptDtlRepository.save(existing);
                            });
                } else if (action == ActionType.isDeleted) {
                    paRptDtlRepository.deleteById(new ShipPrincipalPaRptDtlId(id, paRptDetail.getDetRowId()));
                }
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

    private List<ShipPrincipalPaRptDetailResponseDto> mapPaRptDetailsWithLov(List<ShipPrincipalPaRptDtl> details) {
        if (details == null || details.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, LovItem> portCallRptTypeMap = getLovMap("PORT_CALL_RPT_TYPE");
        Map<Long, LovItem> pdfTemplateMap = getLovMap("PDF_TEMPLATE_MST");
        Map<Long, LovItem> emailTemplateMap = getLovMap("EMAIL_TEMPLATE_MST");
        Map<Long, LovItem> userRolesMap = getLovMap("USER_ROLES");
        Map<String, LovItem> vesselTypeMap = getLovMapByCode("VESSEL_TYPE_MASTER");

        return details.stream().map(entity -> {
            ShipPrincipalPaRptDetailResponseDto dto = mapper.mapToPaRptDetailResponseDTO(entity);
            dto.setPortCallReportType(portCallRptTypeMap.get(entity.getPortCallReportType()));
            dto.setPdfTemplate(pdfTemplateMap.get(entity.getPdfTemplatePoid()));
            dto.setEmailTemplate(emailTemplateMap.get(entity.getEmailTemplatePoid()));
            dto.setAssignedToRole(userRolesMap.get(entity.getAssignedToRolePoid()));
            dto.setEscalationRole1(userRolesMap.get(entity.getEscalationRole1()));
            dto.setEscalationRole2(userRolesMap.get(entity.getEscalationRole2()));
            dto.setVesselType(vesselTypeMap.get(entity.getVesselType()));
            
            return dto;
        }).collect(Collectors.toList());
    }

    private Map<Long, LovItem> getLovMap(String lovName) {
        LovResponse lovResponse = lovService.getLovList(lovName, null, null);
        if (lovResponse != null && lovResponse.getItems() != null) {
            return lovResponse.getItems().stream()
                    .collect(Collectors.toMap(LovItem::getPoid, item -> item));
        }
        return new HashMap<>();
    }

    private Map<String, LovItem> getLovMapByCode(String lovName) {
        LovResponse lovResponse = lovService.getLovList(lovName, null, null);
        if (lovResponse != null && lovResponse.getItems() != null) {
            return lovResponse.getItems().stream()
                    .collect(Collectors.toMap(LovItem::getCode, item -> item));
        }
        return new HashMap<>();
    }

    private List<PaymentItemResponseDTO> mapPaymentsWithLov(List<ShipPrincipalMasterPymtDtl> payments) {
        if (payments == null || payments.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, LovItem> paymentTypeMap = getLovMapByCode("PAYMENT_TYPE");

        return payments.stream().map(entity -> {
            PaymentItemResponseDTO dto = mapper.mapToPaymentResponseDTO(entity);
            dto.setType(paymentTypeMap.get(entity.getType()));
            return dto;
        }).collect(Collectors.toList());
    }

    private List<ChargeDetailDto> mapChargesWithLov(List<ShipPrincipalMasterDtl> charges) {
        if (charges == null || charges.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, LovItem> chargeMasterMap = getLovMap("CHARGE_MASTER");

        return charges.stream().map(entity -> {
            ChargeDetailDto dto = mapper.mapToChargeDTO(entity);
            LovItem chargeItem = chargeMasterMap.get(entity.getChargePoid());
            if (chargeItem != null) {
                dto.setChargeCode(chargeItem.getCode());
                dto.setChargeName(chargeItem.getLabel());
            }
            return dto;
        }).collect(Collectors.toList());
    }
}
