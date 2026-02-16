package com.asg.operations.finaldisbursementaccount.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.enums.LogDetailsEnum;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import com.asg.common.lib.service.PrintService;
import com.asg.operations.common.PageResponse;
import com.asg.operations.commonlov.service.LovService;
import com.asg.operations.exceptions.CustomException;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.finaldisbursementaccount.dto.CreateFdaHeaderRequest;
import com.asg.operations.finaldisbursementaccount.dto.*;
import com.asg.operations.finaldisbursementaccount.entity.PdaFdaDtl;
import com.asg.operations.finaldisbursementaccount.entity.PdaFdaHdr;
import com.asg.operations.finaldisbursementaccount.key.PdaFdaDtlId;
import com.asg.operations.finaldisbursementaccount.repository.FdaCustomRepository;
import com.asg.operations.finaldisbursementaccount.repository.PdaFdaDtlRepository;
import com.asg.operations.finaldisbursementaccount.repository.PdaFdaHdrRepository;
import com.asg.operations.finaldisbursementaccount.util.CalculationUtils;
import com.asg.operations.finaldisbursementaccount.util.ChargesMapper;
import com.asg.operations.finaldisbursementaccount.util.HeaderMapper;
import com.asg.operations.finaldisbursementaccount.util.ValidationUtils;
import com.asg.operations.pdaentryform.entity.PdaEntryHdr;
import com.asg.operations.pdaentryform.repository.PdaEntryHdrRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FdaServiceImpl implements FdaService {

    private final PdaFdaHdrRepository pdaFdaHdrRepository;
    private final PdaFdaDtlRepository pdaFdaDtlRepository;
    private final FdaCustomRepository fdaCustomRepository;
    private final PdaEntryHdrRepository pdaEntryHdrRepository;
    private final ValidationUtils validationUtils;
    private final LovService lovService;
    private final PrintService printService;
    private final javax.sql.DataSource dataSource;
    private final LoggingService loggingService;
    private final DocumentDeleteService documentDeleteService;
    private final DocumentSearchService documentSearchService;

    @Override
    @Transactional(readOnly = true)
    public FdaHeaderDto getFdaHeader(Long transactionPoid, Long groupPoid, Long companyPoid) {

        PdaFdaHdr entity = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        FdaHeaderDto fdaHeaderDto = HeaderMapper.mapHeaderEntityToDto(entity);
        setDetailsForHeader(fdaHeaderDto);

        List<PdaFdaDtl> dtls = pdaFdaDtlRepository.findByIdTransactionPoid(transactionPoid);

        List<FdaChargeDto> charges = dtls.stream()
                .map(ChargesMapper::mapChargeEntityToDto)
                .collect(Collectors.toList());

        for (FdaChargeDto charge : charges) {
            setDetailsForCharge(charge);
        }

        CalculationUtils.computeProfitLossRuntime(charges, fdaHeaderDto);

        fdaHeaderDto.setCharges(charges);

        return fdaHeaderDto;
    }

    @Override
    @Transactional
    public FdaHeaderDto createFdaHeader(CreateFdaHeaderRequest dto, Long groupPoid, Long companyPoid, String userId) {

        validationUtils.validateHeaderBeforeSave(dto);

        // Validate financial year and transaction period before save
        validationUtils.validateFinancialAndTransactionPeriodForCreate(companyPoid, dto.getTransactionDate());

        if (dto.getArrivalDate() != null && dto.getVesselSailDate() != null && dto.getVesselSailDate().isBefore(dto.getArrivalDate())) {
            throw new CustomException("Vessel sail date cannot be before arrival date", 400);
        }

        PdaFdaHdr entity = new PdaFdaHdr();
        HeaderMapper.mapCreateHeaderRequestToEntity(dto, entity, groupPoid, companyPoid, userId);

        // docRef will be generated by database trigger (PDA_FDA_HDR_TRG)
        // If trigger doesn't set it, we set a fallback (though trigger should handle it)
        // Note: The trigger generates docRef based on FDA_SUB_TYPE and PDA_REF
        // For now, we'll let the trigger handle it, but we can set a fallback if needed
        entity = pdaFdaHdrRepository.save(entity);

        if (dto.getCharges() != null && !dto.getCharges().isEmpty()) {
            saveCharges(entity.getTransactionPoid(), dto.getCharges(), userId, groupPoid, companyPoid);
        }

        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, UserContext.getDocumentId(), entity.getTransactionPoid().toString());
        return getFdaHeader(entity.getTransactionPoid(), groupPoid, companyPoid);
    }

    @Override
    @Transactional
    public FdaHeaderDto updateFdaHeader(Long transactionPoid, UpdateFdaHeaderRequest dto, Long groupPoid, Long companyPoid, String userId) {

        PdaFdaHdr entity = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        validationUtils.validateHeaderBeforeUpdate(dto, entity);

        // Validate status - cannot edit if status is CLOSED or HOLD (aligned with legacy DocumentBeforeEdit)
        String status = entity.getStatus();
        if (StringUtils.isNotBlank(status)) {
            if ("CLOSED".equalsIgnoreCase(status)) {
                throw new CustomException("FDA Status is Closed. Cannot update.", 400);
            }
            if ("HOLD".equalsIgnoreCase(status)) {
                throw new CustomException("FDA Status is Hold. Cannot update.", 400);
            }
        }

        // Validate financial year and transaction period before update
        // Note: UpdateFdaHeaderRequest doesn't include transactionDate, so we validate the existing date
        // This prevents updates to records whose transaction date is outside the allowed period
        LocalDate oldTransactionDate = entity.getTransactionDate();
        // Since UpdateFdaHeaderRequest doesn't have transactionDate, newTransactionDate will be null
        // This will trigger validation of the old date only (which is what we want)
        validationUtils.validateFinancialAndTransactionPeriodForUpdate(companyPoid, oldTransactionDate, dto.getTransactionDate());

        PdaFdaHdr oldEntity = new PdaFdaHdr();
        BeanUtils.copyProperties(entity, oldEntity);

        HeaderMapper.mapUpdateHeaderDtoToEntity(dto, entity, userId);
        pdaFdaHdrRepository.save(entity);

        if (dto.getCharges() != null && !dto.getCharges().isEmpty()) {
            saveCharges(transactionPoid, dto.getCharges(), userId, groupPoid, companyPoid);
        }

        loggingService.logChanges(oldEntity, entity, PdaFdaHdr.class, UserContext.getDocumentId(), transactionPoid.toString(), LogDetailsEnum.MODIFIED, "TRANSACTION_POID");
        return getFdaHeader(transactionPoid, groupPoid, companyPoid);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FdaHeaderDto> getFdaList(Long groupPoid, Long companyPoid, Long transactionPoid, String vesselName, LocalDate etaFrom, LocalDate etaTo, Pageable pageable) {

//        can be used in future
//        Page<PdaFdaHdr> page = pdaFdaHdrRepository.searchFdaHeaders(groupPoid, companyPoid, transactionPoid, vesselName, etaFrom, etaTo, pageable);

        Page<PdaFdaHdr> page = pdaFdaHdrRepository.searchFdaHeaders(groupPoid, companyPoid, transactionPoid, etaFrom, etaTo, pageable);

        List<FdaHeaderDto> content = page.getContent().stream()
                .map(HeaderMapper::mapHeaderEntityToDto)
                .collect(Collectors.toList());

        for (FdaHeaderDto fdaHeaderDto : content) {
            setDetailsForHeader(fdaHeaderDto);
        }

        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast(), page.getNumberOfElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAllFdaWithFilters(String documentId, FilterRequestDto filterRequest, Pageable pageable, LocalDate periodFrom, LocalDate periodTo) {

        String operator = documentSearchService.resolveOperator(filterRequest);
        String isDeleted = documentSearchService.resolveIsDeleted(filterRequest);
        List<FilterDto> filters = documentSearchService.resolveDateFilters(filterRequest, "TRANSACTION_DATE", periodFrom, periodTo);

        RawSearchResult raw = documentSearchService.search(documentId, filters, operator, pageable, isDeleted,
                "DOC_REF",
                "TRANSACTION_POID");

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    @Override
    @Transactional
    public void softDeleteFda(Long transactionPoid, String userId, @Valid DeleteReasonDto deleteReasonDto) {

        PdaFdaHdr hdr = pdaFdaHdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        documentDeleteService.deleteDocument(
                transactionPoid,
                "PDA_FDA_HDR",
                "TRANSACTION_POID",
                deleteReasonDto,
                hdr.getTransactionDate()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FdaChargeDto> getCharges(Long transactionPoid, Long groupPoid, Long companyPoid, Pageable pageable) {

        pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        Page<FdaChargeDto> dtoPage = pdaFdaDtlRepository.findByTransactionPoid(transactionPoid, pageable)
                .map(ChargesMapper::mapChargeEntityToDto);

        List<FdaChargeDto> charges = dtoPage.getContent();

        for (FdaChargeDto chargeDto : charges) {
            setDetailsForCharge(chargeDto);
        }

        CalculationUtils.computeProfitLossRuntime(charges, null);

        return new PageResponse<>(
                charges,
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages(),
                dtoPage.isFirst(),
                dtoPage.isLast(),
                dtoPage.getNumberOfElements()
        );
    }

    @Override
    @Transactional
    public void saveCharges(Long transactionPoid, List<FdaChargeDto> chargeDtos, String userId, Long groupPoid, Long companyPoid) {
        // Validate status before saving charges - cannot modify charges if status is CLOSED or HOLD
        PdaFdaHdr hdr = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        String status = hdr.getStatus();
        if (StringUtils.isNotBlank(status)) {
            if ("CLOSED".equalsIgnoreCase(status)) {
                throw new CustomException("FDA Status is Closed. Cannot modify charges.", 400);
            }
            if ("HOLD".equalsIgnoreCase(status)) {
                throw new CustomException("FDA Status is Hold. Cannot modify charges.", 400);
            }
        }

        List<PdaFdaDtl> toSave = new ArrayList<>();

        for (FdaChargeDto dto : chargeDtos) {
            String action = StringUtils.isNotBlank(dto.getActionType()) ? dto.getActionType().toLowerCase() : "";

            switch (action) {
                case "isdeleted":
                    if (dto.getDetRowId() != null) {
                        PdaFdaDtlId id = new PdaFdaDtlId(transactionPoid, dto.getDetRowId());
                        pdaFdaDtlRepository.findById(id).ifPresent(entity -> {
                            if (StringUtils.isNotBlank(entity.getManual()) && "N".equalsIgnoreCase(entity.getManual())) {
                                throw new CustomException("Cannot delete system-generated charge lines", 403);
                            }
                            pdaFdaDtlRepository.delete(entity);
                        });
                    }
                    break;
                case "iscreated":
                case "isupdated":
                    validationUtils.handleCreateOrUpdate(transactionPoid, dto, toSave, userId);
                    break;
                default:
                    // ignore unknown actions
            }
        }

        if (!toSave.isEmpty()) {
            pdaFdaDtlRepository.saveAll(toSave);
        }

        validationUtils.recalculateHeaderTotals(transactionPoid, userId, groupPoid, companyPoid);
    }

    @Override
    @Transactional
    public void deleteCharge(Long transactionPoid, Long detRowId, String userId) {
        PdaFdaDtlId id = new PdaFdaDtlId(transactionPoid, detRowId);

        PdaFdaDtl entity = pdaFdaDtlRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("FDA Detail", "detRowId", detRowId));

        if (StringUtils.isNotBlank(entity.getManual()) && "N".equalsIgnoreCase(entity.getManual())) {
            throw new CustomException("Cannot delete system-generated charge lines", 403);
        }

        pdaFdaDtlRepository.delete(entity);
    }

    @Override
    @Transactional
    public String closeFda(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid) {

        PdaFdaHdr hdr = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        if (StringUtils.isNotBlank(hdr.getStatus()) && "C".equalsIgnoreCase(hdr.getStatus())) {
            throw new CustomException("FDA is already closed.", 400);
        }

        if (hdr.getVesselSailDate() == null) {
            throw new CustomException("Actual Vessel Sail Date is required for closing the FDA.", 400);
        }

        if (StringUtils.isBlank(hdr.getAccountsVerified()) || !"Y".equalsIgnoreCase(hdr.getAccountsVerified())) {
            throw new CustomException("Accounts verification is required before closing the FDA.", 400);
        }

        if (hdr.getTotalAmount() == null || hdr.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            throw new CustomException("FDA total amount is zero. Please use Close Without Amount option.", 400);
        }

        return fdaCustomRepository.closeFda(groupPoid, companyPoid, userPoid, transactionPoid);
    }

    @Override
    @Transactional
    public String reopenFda(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid, FdaReOpenDto fdaReOpenDto) {

        PdaFdaHdr hdr = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        if (StringUtils.isBlank(hdr.getStatus()) || !"CLOSED".equalsIgnoreCase(hdr.getStatus())) {
            throw new CustomException("Only closed FDAs can be reopened.", 400);
        }

        return fdaCustomRepository.reopenFda(groupPoid, companyPoid, userPoid, transactionPoid, fdaReOpenDto.getComment());
    }

    @Override
    @Transactional
    public String submitFda(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid) {

        PdaFdaHdr hdr = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        if (StringUtils.isNotBlank(hdr.getStatus()) && "CLOSED".equalsIgnoreCase(hdr.getStatus())) {
            throw new CustomException("Closed FDAs cannot be submitted.", 400);
        }

        return fdaCustomRepository.submitFda(groupPoid, companyPoid, userPoid, transactionPoid);
    }

    @Override
    @Transactional
    public String verifyFda(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid) {

        PdaFdaHdr hdr = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        if (StringUtils.isNotBlank(hdr.getStatus()) && "CLOSED".equalsIgnoreCase(hdr.getStatus())) {
            throw new CustomException("Closed FDAs cannot be verified.", 400);
        }

        hdr.setAccountsVerified("Y");
        hdr.setLastModifiedBy(String.valueOf(userPoid));
        hdr.setLastModifiedDate(LocalDateTime.now());
        pdaFdaHdrRepository.save(hdr);

        return fdaCustomRepository.verifyFda(groupPoid, companyPoid, userPoid, transactionPoid);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String returnFda(Long groupPoid, Long companyPoid, Long userPoid,
                            Long transactionPoid, String correctionRemarks) {
        if (StringUtils.isBlank(correctionRemarks)) {
            throw new CustomException("Correction remarks are required", 400);
        }

        PdaFdaHdr hdr = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA", "Transaction Poid", transactionPoid));

        hdr.setOpsCorrectionRemarks(correctionRemarks);
        hdr.setOpsReturnedDate(LocalDate.now());
        hdr.setAccountsVerified("N");
        hdr.setLastModifiedBy("SYSTEM");
        hdr.setLastModifiedDate(LocalDateTime.now());

        pdaFdaHdrRepository.save(hdr);

        return fdaCustomRepository.returnFda(groupPoid, companyPoid, userPoid, transactionPoid, correctionRemarks);
    }

    @Override
    @Transactional
    public String supplementaryFda(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid) {
        return fdaCustomRepository.supplementaryFda(groupPoid, companyPoid, userPoid, transactionPoid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FdaSupplementaryInfoDto> getSupplementaryInfo(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        return fdaCustomRepository.getSupplementaryInfo(transactionPoid, groupPoid, companyPoid, userPoid);
    }

    @Override
    @Transactional
    public String closeFdaWithoutAmount(Long transactionPoid, Long groupPoid, Long companyPoid,
                                        Long userPoid, String closedRemark) {

        PdaFdaHdr hdr = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        if (StringUtils.isNotBlank(hdr.getStatus()) && "CLOSED".equalsIgnoreCase(hdr.getStatus())) {
            throw new CustomException("FDA is already closed.", 400);
        }

        if (StringUtils.isBlank(closedRemark)) {
            throw new CustomException("Closed remarks are required for closing FDA without amount.", 400);
        }

        if (hdr.getVesselSailDate() == null) {
            throw new CustomException("Actual Vessel Sail Date is required for closing the FDA.", 400);
        }

        if (StringUtils.isBlank(hdr.getAccountsVerified()) || !"Y".equalsIgnoreCase(hdr.getAccountsVerified())) {
            throw new CustomException("Accounts verification is required before closing the FDA.", 400);
        }

        if (hdr.getTotalAmount() != null && hdr.getTotalAmount().compareTo(BigDecimal.ZERO) != 0) {
            throw new CustomException("Close Without Amount is allowed only when FDA total amount is zero.", 400);
        }

        return fdaCustomRepository.closeFdaWithoutAmount(transactionPoid, groupPoid, companyPoid, userPoid, closedRemark);
    }

    @Override
    @Transactional(readOnly = true)
    public PartyGlResponse getPartyGl(Long groupPoid, Long companyPoid, Long userPoid, Long partyPoid, String partyType) {
        return fdaCustomRepository.getPartyGl(groupPoid, companyPoid, userPoid, partyPoid, partyType);
    }

    @Override
    @Transactional
    public String createFdaFromPda(Long groupPoid, Long companyPoid, Long userPoid, Long pdaTransactionPoid) {
        return fdaCustomRepository.createFdaFromPda(groupPoid, companyPoid, userPoid, pdaTransactionPoid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PdaLogResponse> getPdaLogs(Long transactionPoid, Long groupPoid, Long companyPoid) {

        Optional<PdaFdaHdr> fdaHdrOptional = pdaFdaHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoidAndDeleted(transactionPoid, groupPoid, companyPoid, "N");

        if (fdaHdrOptional.isEmpty()) {
            return List.of();
        }
        PdaFdaHdr fdaHeader = fdaHdrOptional.get();

        List<PdaFdaHdr> logs = pdaFdaHdrRepository
                .findByPdaRefAndGroupPoidAndCompanyPoidAndDeleted(fdaHeader.getPdaRef(), groupPoid, companyPoid, "N");

        if (logs.isEmpty()) {
            return List.of();
        }

        List<Long> pdaTransactionIds = logs.stream()
                .map(fda -> {
                    try {
                        return Long.valueOf(fda.getPdaRef());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<String, PdaEntryHdr> pdaMap = pdaEntryHdrRepository.findAllById(pdaTransactionIds).stream()
                .collect(Collectors.toMap(pda -> String.valueOf(pda.getTransactionPoid()), pda -> pda));

        return logs.stream().map(fda -> {
            PdaLogResponse response = new PdaLogResponse();

            PdaEntryHdr pda = pdaMap.get(fda.getPdaRef());
            if (pda != null) {
                response.setPdaTransactionPoid(pda.getTransactionPoid());
                response.setPdaDocRef(pda.getDocRef());
                response.setPdaTransactionDate(pda.getTransactionDate());
            } else {
                response.setPdaTransactionPoid(null);
                response.setPdaDocRef(fda.getPdaRef());
                response.setPdaTransactionDate(null);
            }

            response.setFdaTransactionPoid(fda.getTransactionPoid());
            response.setFdaDocRef(fda.getDocRef());
            response.setFdaTransactionDate(fda.getTransactionDate());
            response.setFdaStatus(fda.getStatus());

            response.setDocumentSubmittedDate(fda.getDocumentSubmittedDate());
            response.setDocumentSubmittedBy(fda.getDocumentSubmittedBy());
            response.setDocumentSubmittedStatus(fda.getDocumentSubmittedStatus());

            response.setVerificationAcceptedDate(fda.getVerificationAcceptedDate());
            response.setVerificationAcceptedBy(fda.getVerificationAcceptedBy());
            response.setDocumentReceivedStatus(fda.getDocumentReceivedStatus());

            response.setCreatedBy(fda.getCreatedBy());
            response.setCreatedDate(fda.getCreatedDate());
            response.setLastModifiedBy(fda.getLastModifiedBy());
            response.setLastModifiedDate(fda.getLastModifiedDate());

            return response;
        }).collect(Collectors.toList());
    }

    private void setDetailsForHeader(FdaHeaderDto fdaHeaderDto) {

        fdaHeaderDto.setGroupDet(lovService.getLovItemByPoid(fdaHeaderDto.getGroupPoid(), "GROUP", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setCompanyDet(lovService.getLovItemByPoid(fdaHeaderDto.getCompanyPoid(), "COMPANY", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setPrincipalDet(lovService.getLovItemByPoid(fdaHeaderDto.getPrincipalPoid(), "PRINCIPAL_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setVoyageDet(lovService.getLovItemByPoid(fdaHeaderDto.getVoyagePoid(), "VESSAL_VOYAGE", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setVesselDet(lovService.getLovItemByPoid(fdaHeaderDto.getVesselPoid(), "VESSEL_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setPortDet(lovService.getLovItemByPoid(fdaHeaderDto.getPortPoid(), "PDA_PORT_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setAddressDet(lovService.getLovItemByPoid(fdaHeaderDto.getAddressPoid(), "ADDRESS_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setSalesmanDet(lovService.getLovItemByPoid(fdaHeaderDto.getSalesmanPoid(), "SALESMAN", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setTermsDet(lovService.getLovItemByPoid(fdaHeaderDto.getTermsPoid(), "TERMS_TEMPLATE_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setVesselTypeDet(lovService.getLovItemByPoid(StringUtils.isNotBlank(fdaHeaderDto.getVesselTypePoid()) ? Long.valueOf(fdaHeaderDto.getVesselTypePoid()) : null, "VESSEL_TYPE_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setLineDet(lovService.getLovItemByPoid(fdaHeaderDto.getLinePoid(), "LINE_MASTER_ALL", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setPrintBankDet(lovService.getLovItemByPoid(fdaHeaderDto.getPrintBankPoid(), "BANK_MASTER_COMPANYWISE", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setVesselHandledByDet(lovService.getLovItemByPoid(fdaHeaderDto.getVesselHandledBy(), "PDA_USER_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setCommodityDet(lovService.getLovItemByCode(fdaHeaderDto.getCommodityPoid(), "COMODITY", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setNominatedPartyTypeDet(lovService.getLovItemByCode(fdaHeaderDto.getNominatedPartyType(), "PDA_NOMINATED_PARTY_TYPE", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setOperationTypeDet(lovService.getLovItemByCode(fdaHeaderDto.getOperationType(), "PDA_OPERATION_TYPES", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setUnitDet(lovService.getLovItemByCode(fdaHeaderDto.getUnit(), "UNIT_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setPdaRefDet(lovService.getLovItemByCode(fdaHeaderDto.getPdaRef(), "PROCESS_PDA", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        if (StringUtils.isNotBlank(fdaHeaderDto.getNominatedPartyType()) && "CUSTOMER".equalsIgnoreCase(fdaHeaderDto.getNominatedPartyType())) {
            fdaHeaderDto.setNominatedPartyDet(lovService.getLovItemByPoid(fdaHeaderDto.getNominatedPartyPoid(), "PDA_NOMINATED_PARTY_CUSTOMER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        }
        if (StringUtils.isNotBlank(fdaHeaderDto.getNominatedPartyType()) && "PRINCIPAL".equalsIgnoreCase(fdaHeaderDto.getNominatedPartyType())) {
            fdaHeaderDto.setNominatedPartyDet(lovService.getLovItemByPoid(fdaHeaderDto.getNominatedPartyPoid(), "PDA_NOMINATED_PARTY_PRINCIPAL", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        }
    }

    private void setDetailsForCharge(FdaChargeDto charge) {
        charge.setChargeDet(lovService.getLovItemByPoid(charge.getChargePoid(), "CHARGE_MASTER_FOR_PDA", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        charge.setRateTypeDet(lovService.getLovItemByPoid(charge.getRateTypePoid(), "PDA_RATE_TYPE_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        charge.setPrincipalDet(lovService.getLovItemByPoid(charge.getPrincipalPoid(), "PRINCIPAL_MASTER_FOR_PDA", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        charge.setPdaDet(lovService.getLovItemByPoid(charge.getPdaPoid(), "PROCESS_PDA", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        charge.setDetailsFromDet(lovService.getLovItemByCode(charge.getDetailsFrom(), "FDA_DETAIL", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
    }

    @Override
    public byte[] printFda(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid, String currency) throws Exception {
        try {
            Map<String, Object> params = printService.buildBaseParams(transactionPoid, "110-161");

            params.put("SUB_HEADER", printService.load("Templates/DocHeaderSubReport.jrxml"));
            params.put("SUB_FOOTER", printService.load("Templates/DocFooterSubReport.jrxml"));

            String reportName;
            if ("USD".equalsIgnoreCase(currency)) {
                params.put("SUB_FDA_DETAIL", printService.load("PDA/FDA_Subreport_USD_ProcCall.jrxml"));
                reportName = "PDA/FDAreportUSD.jrxml";
            } else {
                params.put("SUB_FDA_DETAIL", printService.load("PDA/FDA_Subreport_ProcCall.jrxml"));
                reportName = "PDA/FDAreport1.jrxml";
            }

            net.sf.jasperreports.engine.JasperReport mainReport = printService.load(reportName);
            return printService.fillReportToPdf(mainReport, params, dataSource);

        } catch (RuntimeException e) {
            throw new RuntimeException("FDA PDF generation failed: " + e.getMessage(), e);
        }
    }

}
