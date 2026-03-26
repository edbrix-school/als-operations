package com.asg.operations.shipprincipal.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.operations.commonlov.service.LovService;
import com.asg.operations.crew.dto.ValidationError;
import com.asg.operations.exceptions.CustomException;
import com.asg.operations.exceptions.ResourceAlreadyExistsException;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.exceptions.ValidationException;
import com.asg.operations.shipprincipal.dto.*;
import com.asg.operations.commonlov.dto.LovItem;
import com.asg.operations.commonlov.dto.LovResponse;
import com.asg.operations.shipprincipal.entity.*;
import com.asg.operations.shipprincipal.repository.*;
import com.asg.operations.shipprincipal.util.PrincipalMasterMapper;
import com.asg.operations.portcallreport.enums.ActionType;
import com.asg.operations.vesseltype.entity.VesselType;
import com.asg.operations.vesseltype.repository.VesselTypeRepository;
import com.asg.operations.user.entity.User;
import com.asg.operations.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final LoggingService loggingService;
    private final DocumentDeleteService documentDeleteService;
    private final DocumentSearchService documentSearchService;


    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAllPrincipalsWithFilters(
            String documentId, FilterRequestDto filterRequestDto, Pageable pageable, LocalDate periodFrom, LocalDate periodTo) {

        String operator = documentSearchService.resolveOperator(filterRequestDto);
        String isDeleted = documentSearchService.resolveIsDeleted(filterRequestDto);
        List<FilterDto> filters = documentSearchService.resolveDateFilters(filterRequestDto,"TRANSACTION_DATE", periodFrom, periodTo);

        RawSearchResult raw = documentSearchService.search(documentId, filters, operator, pageable, isDeleted,
                "PRINCIPAL_NAME",
                "PRINCIPAL_POID");

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());

    }

    @Override
    public PrincipalMasterDto getPrincipal(Long id) {
        log.debug("Fetching principal with id: {}", id);
        ShipPrincipalMaster principal = principalRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Principal not found with id: {}", id);
                    return new ResourceNotFoundException("Principal", "Principal Poid", id);
                });

        PrincipalMasterDto dto = mapper.mapToDetailDTO(principal);


        dto.setCountryDet(lovService.getLovItemByPoid(principal.getCountryPoid(), "COUNTRY",
                principal.getGroupPoid(), principal.getCompanyPoid(), UserContext.getUserPoid()));

        dto.setGlCodeDet(lovService.getLovItemByPoid(principal.getGlCodePoid(), "GL_MASTER_LEDGERS",
                principal.getGroupPoid(), principal.getCompanyPoid(), UserContext.getUserPoid()));

        dto.setCompanyDet(lovService.getLovItemByPoid(principal.getCompanyPoid(), "COMPANY",
                principal.getGroupPoid(), principal.getCompanyPoid(), UserContext.getUserPoid()));

        dto.setTaxSlabDet(lovService.getLovItemByCode(principal.getTaxSlab(), "TAX_SLAB",
                principal.getGroupPoid(), principal.getCompanyPoid(), UserContext.getUserPoid()));

        dto.setAddressDet(lovService.getLovItemByPoid(principal.getAddressPoid(), "ADDRESS_MASTER",
                principal.getGroupPoid(), principal.getCompanyPoid(), UserContext.getUserPoid()));
        
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
    public PrincipalMasterDto createPrincipal(@Valid PrincipalCreateDTO dto, Long groupPoid, Long userPoid) {
        log.info("Creating principal with name: {}", dto.getPrincipalName());

        if (principalRepository.existsByPrincipalName(dto.getPrincipalName())) {
            log.error("Principal name already exists: {}", dto.getPrincipalName());
            throw new ResourceAlreadyExistsException("Principal Name", dto.getPrincipalName());
        }

        User user = userRepository.findByUserPoid(userPoid).orElseThrow(() -> new ResourceNotFoundException("User", "user poid", userPoid));

        log.debug("Creating principal with code: {}", dto.getPrincipalCode());
        Long addressPoid = null;
        AddressMaster addressMasterToUpdate = null;
        if (dto.getAddressPoid() == null) {
            if (StringUtils.isBlank(dto.getPrincipalName())) {
                throw new CustomException("Address Name is required for creating new address", 400);
            }
            boolean addressExists = addressMasterRepository.existsByAddressNameIgnoreCaseAndGroupPoid(dto.getPrincipalName(), groupPoid);
            if (addressExists) {
                throw new ResourceAlreadyExistsException("Address Name", dto.getPrincipalName());
            }
            AddressMaster newAddressMaster = new AddressMaster();
            newAddressMaster.setAddressName(dto.getPrincipalName());
            newAddressMaster.setGroupPoid(groupPoid);
            newAddressMaster.setSeqno(Long.valueOf(dto.getSeqNo()));
            addressMasterRepository.save(newAddressMaster);

            dto.setAddressPoid(newAddressMaster.getAddressMasterPoid());
            addressPoid = dto.getAddressPoid();
            addressMasterToUpdate = newAddressMaster;
        } else {
            AddressMaster addressMaster = addressMasterRepository.findByAddressMasterPoid(dto.getAddressPoid());
            addressPoid = addressMaster.getAddressMasterPoid();
            addressMasterToUpdate = addressMaster;
        }

        ShipPrincipalMaster principal = new ShipPrincipalMaster();
        mapper.mapCreateDTOToEntity(dto, principal, groupPoid);

        principal.setAddressPoid(addressPoid);
        principal = principalRepository.save(principal);

        Long principalId = principal.getPrincipalPoid();

        if (dto.getAddressTypeMap() != null && addressMasterToUpdate != null) {
            addressMasterService.saveAllDetails(dto.getAddressTypeMap(), addressMasterToUpdate, user.getUserName(), principalId.toString());
        }

        if (dto.getCharges() != null && !dto.getCharges().isEmpty()) {
            long nextDetRowId = chargeRepository.findMaxDetRowIdByPrincipalPoid(principalId) + 1;
            for (ChargeDetailDto charge : dto.getCharges()) {
                ShipPrincipalMasterDtl entity = new ShipPrincipalMasterDtl();
                entity.setPrincipalPoid(principalId);
                entity.setDetRowId(nextDetRowId++);
                entity.setChargePoid(charge.getChargePoid());
                entity.setRate(charge.getRate());
                entity.setRemarks(charge.getRemarks());
                chargeRepository.save(entity);
                String logDetail = String.format("Row Created on Principal Charge with detRowId: %s", entity.getDetRowId());
                loggingService.createLogSummaryEntry(UserContext.getDocumentId(), principalId.toString() , logDetail);
            }
        }

        if (dto.getPayments() != null && !dto.getPayments().isEmpty()) {
            long nextDetRowId = paymentRepository.findMaxDetRowIdByPrincipalPoid(principalId) + 1;
            for (PaymentItemDTO payment : dto.getPayments()) {
                ShipPrincipalMasterPymtDtl entity = new ShipPrincipalMasterPymtDtl();
                entity.setPrincipalPoid(principalId);
                entity.setDetRowId(nextDetRowId++);
                mapper.mapPaymentDTOToEntity(payment, entity);
                paymentRepository.save(entity);
                String logDetail = String.format("Row Created on Principal Payment with detRowId: %s", entity.getDetRowId());
                loggingService.createLogSummaryEntry(UserContext.getDocumentId(), principalId.toString() , logDetail);
            }
        }

        if (dto.getPortActivityReportDetails() != null && !dto.getPortActivityReportDetails().isEmpty()) {
            log.debug("Processing {} port activity report details", dto.getPortActivityReportDetails().size());
            List<Long> validVesselTypePoids = vesselTypeRepository.findAll().stream()
                    .map(VesselType::getVesselTypePoid)
                    .toList();

            long nextDetRowId = paRptDtlRepository.findMaxDetRowIdByPrincipalPoid(principalId) + 1;
            int index = 0;
            for (ShipPrincipalPaRptDetailDto paRptDetail : dto.getPortActivityReportDetails()) {
                if (paRptDetail.getVesselType() != null && !paRptDetail.getVesselType().isEmpty()) {
                    for (String vesselTypeId : paRptDetail.getVesselType()) {
                        if (!validVesselTypePoids.contains(Long.parseLong(vesselTypeId))) {
                            log.error("Invalid vessel type POID: {}", vesselTypeId);
                            throw new ValidationException("Invalid vessel type", List.of(new ValidationError(index, "vesselType", "Invalid vessel type POID: " + vesselTypeId)));
                        }
                    }
                }
                index++;

                ShipPrincipalPaRptDtl entity = new ShipPrincipalPaRptDtl();
                entity.setPrincipalPoid(principalId);
                entity.setDetRowId(nextDetRowId++);
                entity.setPortCallReportType(paRptDetail.getPortCallReportType());
                entity.setPdfTemplatePoid(paRptDetail.getPdfTemplatePoid());
                entity.setEmailTemplatePoid(paRptDetail.getEmailTemplatePoid());
                entity.setAssignedToRolePoid(paRptDetail.getAssignedToRolePoid());
                entity.setVesselType(paRptDetail.getVesselType() != null ? String.join(",", paRptDetail.getVesselType()) : null);
                entity.setResponseTimeHrs(paRptDetail.getResponseTimeHrs());
                entity.setFrequenceHrs(paRptDetail.getFrequenceHrs());
                entity.setEscalationRole1(paRptDetail.getEscalationRole1());
                entity.setEscalationRole2(paRptDetail.getEscalationRole2());
                entity.setRemarks(paRptDetail.getRemarks());
                paRptDtlRepository.save(entity);
                String logDetail = String.format("Row Created on Principal Port Report Activity with detRowId: %s", entity.getDetRowId());
                loggingService.createLogSummaryEntry(UserContext.getDocumentId(), principalId.toString() , logDetail);
            }
        }

        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, UserContext.getDocumentId(), principalId.toString());
        log.info("Successfully created principal with id: {}", principalId);
        return getPrincipal(principalId);
    }

    @Override
    @Transactional
    public PrincipalMasterDto updatePrincipal(Long id, @Valid PrincipalUpdateDTO dto, Long groupPoid, Long userPoid) {
        log.info("Updating principal with id: {}", id);
        ShipPrincipalMaster principal = principalRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Principal not found with id: {}", id);
                    return new ResourceNotFoundException("Principal", "Principal Poid", id);
                });

        ShipPrincipalMaster oldPrincipal = new ShipPrincipalMaster();
        BeanUtils.copyProperties(principal, oldPrincipal);

        mapper.mapUpdateDTOToEntity(dto, principal, groupPoid);

        User user = userRepository.findByUserPoid(userPoid).orElseThrow(() -> new ResourceNotFoundException("User", "user poid", userPoid));
        Long addressPoid = null;
        if (dto.getAddressPoid() == null) {
            if (StringUtils.isBlank(dto.getPrincipalName())) {
                throw new CustomException("Address Name is required for creating new address", 400);
            }
            boolean addressExists = addressMasterRepository.existsByAddressNameIgnoreCaseAndGroupPoid(dto.getPrincipalName(), groupPoid);
            if (addressExists) {
                throw new ResourceAlreadyExistsException("Address Name", dto.getPrincipalName());
            }
            AddressMaster newAddressMaster = new AddressMaster();
            newAddressMaster.setAddressName(dto.getPrincipalName());
            newAddressMaster.setGroupPoid(groupPoid);
            newAddressMaster.setSeqno(Long.valueOf(dto.getSeqNo()));
            addressMasterRepository.save(newAddressMaster);

            dto.setAddressPoid(newAddressMaster.getAddressMasterPoid());
            addressPoid = dto.getAddressPoid();

            if (dto.getAddressTypeMap() != null) {
                addressMasterService.saveAllDetails(dto.getAddressTypeMap(), newAddressMaster, user.getUserName(), id.toString());
            }
        } else {
            AddressMaster addressMaster = addressMasterRepository.findByAddressMasterPoid(dto.getAddressPoid());
            addressPoid = addressMaster.getAddressMasterPoid();
            if (dto.getAddressTypeMap() != null) {
                addressMasterService.saveAllDetails(dto.getAddressTypeMap(), addressMaster, user.getUserName(), id.toString());
            }
        }

        principal.setAddressPoid(addressPoid);
        principalRepository.save(principal);

        if (dto.getCharges() != null) {
            for (ChargeDetailDto charge : dto.getCharges()) {
                ActionType action = charge.getActionType();

                if (action == null) {
                    continue;
                }

                if (action == ActionType.isCreated) {
                    Long nextDetRowId = chargeRepository.findMaxDetRowIdByPrincipalPoid(id) + 1;
                    ShipPrincipalMasterDtl entity = new ShipPrincipalMasterDtl();
                    entity.setPrincipalPoid(id);
                    entity.setDetRowId(nextDetRowId);
                    entity.setChargePoid(charge.getChargePoid());
                    entity.setRate(charge.getRate());
                    entity.setRemarks(charge.getRemarks());
                    chargeRepository.save(entity);
                    String logDetail = String.format("Row Created on Principal Charge Detail with detRowId: %s", nextDetRowId);
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), id.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    chargeRepository.findById(new ShipPrincipalMasterDtlId(id, charge.getDetRowId()))
                            .ifPresent(existing -> {
                                ShipPrincipalMasterDtl oldCharge = new ShipPrincipalMasterDtl();
                                BeanUtils.copyProperties(existing, oldCharge);
                                existing.setChargePoid(charge.getChargePoid());
                                existing.setRate(charge.getRate());
                                existing.setRemarks(charge.getRemarks());
                                existing = chargeRepository.save(existing);
                                String logDetail = String.format("KeyId = PRINCIPAL_POID %s: DET_ROW_ID %s", existing.getPrincipalPoid(), existing.getDetRowId());
                                loggingService.createLog(oldCharge, existing, ShipPrincipalMasterDtl.class, UserContext.getDocumentId(), id.toString(), logDetail);
                            });
                } else if (action == ActionType.isDeleted) {
                    chargeRepository.deleteById(new ShipPrincipalMasterDtlId(id, charge.getDetRowId()));
                    loggingService.logDelete(charge, UserContext.getDocumentId(), id.toString());
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
                    paymentRepository.save(entity);
                    String logDetail = String.format("Row Created on Principal Payment Detail with detRowId: %s", nextDetRowId);
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), id.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    paymentRepository.findById(new ShipPrincipalMasterDtlId(id, payment.getDetRowId()))
                            .ifPresent(existing -> {
                                ShipPrincipalMasterPymtDtl oldPayment = new ShipPrincipalMasterPymtDtl();
                                BeanUtils.copyProperties(existing, oldPayment);
                                mapper.mapPaymentDTOToEntity(payment, existing);
                                existing = paymentRepository.save(existing);
                                String logDetail = String.format("KeyId = PRINCIPAL_POID %s: DET_ROW_ID %s", existing.getPrincipalPoid(), existing.getDetRowId());
                                loggingService.createLog(oldPayment, existing, ShipPrincipalMasterPymtDtl.class, UserContext.getDocumentId(), id.toString(), logDetail);
                            });
                } else if (action == ActionType.isDeleted) {
                    paymentRepository.deleteById(new ShipPrincipalMasterDtlId(id, payment.getDetRowId()));
                    loggingService.logDelete(payment, UserContext.getDocumentId(), id.toString());
                }
            }
        }

        if (dto.getPortActivityReportDetails() != null) {
            log.debug("Updating {} port activity report details", dto.getPortActivityReportDetails().size());
            List<Long> validVesselTypePoids = vesselTypeRepository.findAll().stream()
                    .map(VesselType::getVesselTypePoid)
                    .toList();

            int index = 0;
            for (ShipPrincipalPaRptDetailDto paRptDetail : dto.getPortActivityReportDetails()) {
                if (paRptDetail.getVesselType() != null && !paRptDetail.getVesselType().isEmpty()) {
                    for (String vesselTypeId : paRptDetail.getVesselType()) {
                        if (!validVesselTypePoids.contains(Long.parseLong(vesselTypeId))) {
                            log.error("Invalid vessel type POID: {}", vesselTypeId);
                            throw new ValidationException("Invalid vessel type", List.of(new ValidationError(index, "vesselType", "Invalid vessel type POID: " + vesselTypeId)));
                        }
                    }
                }
                index++;

                ActionType action = paRptDetail.getActionType();

                if (action == null) {
                    continue;
                }

                if (action == ActionType.isCreated) {
                    Long nextDetRowId = paRptDtlRepository.findMaxDetRowIdByPrincipalPoid(id) + 1;
                    ShipPrincipalPaRptDtl entity = new ShipPrincipalPaRptDtl();
                    entity.setPrincipalPoid(id);
                    entity.setDetRowId(nextDetRowId);
                    entity.setPortCallReportType(paRptDetail.getPortCallReportType());
                    entity.setPdfTemplatePoid(paRptDetail.getPdfTemplatePoid());
                    entity.setEmailTemplatePoid(paRptDetail.getEmailTemplatePoid());
                    entity.setAssignedToRolePoid(paRptDetail.getAssignedToRolePoid());
                    entity.setVesselType(paRptDetail.getVesselType() != null ? String.join(",", paRptDetail.getVesselType()) : null);
                    entity.setResponseTimeHrs(paRptDetail.getResponseTimeHrs());
                    entity.setFrequenceHrs(paRptDetail.getFrequenceHrs());
                    entity.setEscalationRole1(paRptDetail.getEscalationRole1());
                    entity.setEscalationRole2(paRptDetail.getEscalationRole2());
                    entity.setRemarks(paRptDetail.getRemarks());
                    paRptDtlRepository.save(entity);
                    String logDetail = String.format("Row Created on Principal Port Activity Report Detail with detRowId: %s", nextDetRowId);
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), id.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    paRptDtlRepository.findById(new ShipPrincipalPaRptDtlId(id, paRptDetail.getDetRowId()))
                            .ifPresent(existing -> {
                                ShipPrincipalPaRptDtl oldPaRpt = new ShipPrincipalPaRptDtl();
                                BeanUtils.copyProperties(existing, oldPaRpt);
                                existing.setPortCallReportType(paRptDetail.getPortCallReportType());
                                existing.setPdfTemplatePoid(paRptDetail.getPdfTemplatePoid());
                                existing.setEmailTemplatePoid(paRptDetail.getEmailTemplatePoid());
                                existing.setAssignedToRolePoid(paRptDetail.getAssignedToRolePoid());
                                existing.setVesselType(paRptDetail.getVesselType() != null ? String.join(",", paRptDetail.getVesselType()) : null);
                                existing.setResponseTimeHrs(paRptDetail.getResponseTimeHrs());
                                existing.setFrequenceHrs(paRptDetail.getFrequenceHrs());
                                existing.setEscalationRole1(paRptDetail.getEscalationRole1());
                                existing.setEscalationRole2(paRptDetail.getEscalationRole2());
                                existing.setRemarks(paRptDetail.getRemarks());
                                existing = paRptDtlRepository.save(existing);

                                String logDetail = String.format("KeyId = PRINCIPAL_POID %s: DET_ROW_ID %s", existing.getPrincipalPoid(), existing.getDetRowId());
                                loggingService.createLog(oldPaRpt, existing, ShipPrincipalPaRptDtl.class, UserContext.getDocumentId(), id.toString(), logDetail);
                            });
                } else if (action == ActionType.isDeleted) {
                    paRptDtlRepository.deleteById(new ShipPrincipalPaRptDtlId(id, paRptDetail.getDetRowId()));
                    loggingService.logDelete(paRptDetail, UserContext.getDocumentId(), id.toString());
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

        loggingService.logChanges(oldPrincipal, principal, ShipPrincipalMaster.class, UserContext.getDocumentId(), id.toString(), LogDetailsEnum.MODIFIED, "PRINCIPAL_POID");
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
                    return new ResourceNotFoundException("Principal", "Principal Poid", id);
                });
        String newStatus = "Y".equals(principal.getActive()) ? "N" : "Y";
        principal.setActive(newStatus);
        principalRepository.save(principal);
        log.info("Successfully toggled active status to {} for principal with id: {}", newStatus, id);
    }

    @Override
    @Transactional
    public void deletePrincipal(Long id, @Valid DeleteReasonDto deleteReasonDto) {
        log.info("Soft deleting principal with id: {}", id);

        ShipPrincipalMaster principal = principalRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Principal", "id", id));

        documentDeleteService.deleteDocument(
                id,
                "SHIP_PRINCIPAL_MASTER",
                "PRINCIPAL_POID",
                deleteReasonDto,
                LocalDate.now()
        );
    }


    @Override
    @Transactional
    public CreateLedgerResponseDto createLedger(Long principalPoid,
                                                Long groupPoid,
                                                Long companyPoid,
                                                Long userPoid) {
        // Get principal
        ShipPrincipalMaster principal = principalRepository.findByIdAndNotDeleted(principalPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Principal", "principalPoid", principalPoid));

        User user = userRepository.findByUserPoid(userPoid).orElseThrow(() -> new ResourceNotFoundException("User", "user poid", userPoid));

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
        Map<Long, LovItem> vesselTypeMap = getLovMap("VESSEL_TYPE_MASTER");

        return details.stream().map(entity -> {
            ShipPrincipalPaRptDetailResponseDto dto = mapper.mapToPaRptDetailResponseDTO(entity);
            dto.setPortCallReportTypePoid(entity.getPortCallReportType());
            dto.setPortCallReportTypeDet(portCallRptTypeMap.get(entity.getPortCallReportType()));
            dto.setPdfTemplatePoid(entity.getPdfTemplatePoid());
            dto.setPdfTemplateDet(pdfTemplateMap.get(entity.getPdfTemplatePoid()));
            dto.setEmailTemplatePoid(entity.getEmailTemplatePoid());
            dto.setEmailTemplateDet(emailTemplateMap.get(entity.getEmailTemplatePoid()));
            dto.setAssignedToRolePoid(entity.getAssignedToRolePoid());
            dto.setAssignedToRoleDet(userRolesMap.get(entity.getAssignedToRolePoid()));
            
            if (entity.getVesselType() != null && !entity.getVesselType().isEmpty()) {
                List<Long> vesselTypePoids = Arrays.stream(entity.getVesselType().split(","))
                        .map(String::trim)
                        .map(Long::parseLong)
                        .toList();
                dto.setVesselTypePoids(vesselTypePoids);
                dto.setVesselTypeDets(vesselTypePoids.stream()
                        .map(vesselTypeMap::get)
                        .filter(Objects::nonNull)
                        .toList());
            }
            
            dto.setEscalationRole1Poid(entity.getEscalationRole1());
            dto.setEscalationRole1Det(userRolesMap.get(entity.getEscalationRole1()));
            dto.setEscalationRole2Poid(entity.getEscalationRole2());
            dto.setEscalationRole2Det(userRolesMap.get(entity.getEscalationRole2()));

            return dto;
        }).collect(Collectors.toList());
    }

    private Map<Long, LovItem> getLovMap(String lovName) {
        LovResponse lovResponse = lovService.getLovList(lovName, null, null, null, null, null);
        if (lovResponse != null && lovResponse.getItems() != null) {
            return lovResponse.getItems().stream()
                    .collect(Collectors.toMap(LovItem::getPoid, item -> item));
        }
        return new HashMap<>();
    }

    private Map<String, LovItem> getLovMapByCode(String lovName) {
        LovResponse lovResponse = lovService.getLovList(lovName, null, null, null, null, null);
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
        Map<Long, LovItem> countryMap = getLovMap("COUNTRY");

        return payments.stream().map(entity -> {
            PaymentItemResponseDTO dto = mapper.mapToPaymentResponseDTO(entity);
            dto.setType(entity.getType());
            dto.setTypeDet(paymentTypeMap.get(entity.getType()));
            dto.setBeneficiaryCountryDet(countryMap.get(entity.getBeneficiaryCountry()));
            dto.setIntermediaryCountryDet(countryMap.get(entity.getIntermediaryCountryPoid()));
            return dto;
        }).collect(Collectors.toList());
    }

    private List<ChargeDetailResponseDto> mapChargesWithLov(List<ShipPrincipalMasterDtl> charges) {
        if (charges == null || charges.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, LovItem> chargeMasterMap = getLovMap("CHARGE_MASTER");

        return charges.stream().map(entity -> {
            ChargeDetailResponseDto dto = mapper.mapToChargeResponseDTO(entity);
            LovItem chargeItem = chargeMasterMap.get(entity.getChargePoid());
            if (chargeItem != null) {
                dto.setChargeDet(chargeItem);
            }
            return dto;
        }).collect(Collectors.toList());
    }
}
