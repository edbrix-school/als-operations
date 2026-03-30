package com.asg.operations.pdaporttariffmaster.service;

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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import com.asg.operations.pdaporttariffmaster.dto.*;
import com.asg.operations.pdaporttariffmaster.entity.PdaPortTariffChargeDtl;
import com.asg.operations.pdaporttariffmaster.entity.PdaPortTariffHdr;
import com.asg.operations.pdaporttariffmaster.entity.PdaPortTariffSlabDtl;
import com.asg.operations.pdaporttariffmaster.key.PdaPortTariffChargeDtlId;
import com.asg.operations.pdaporttariffmaster.key.PdaPortTariffSlabDtlId;
import com.asg.operations.pdaporttariffmaster.repository.*;
import com.asg.operations.portcallreport.enums.ActionType;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.pdaporttariffmaster.util.PdaPortTariffMapper;
import jakarta.persistence.EntityManager;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class PdaPortTariffHdrServiceImpl implements PdaPortTariffHdrService {

    private final PdaRateTypeMasterRepository pdaRateTypeMasterRepository;
    private final PdaPortTariffHdrRepository tariffHdrRepository;
    private final PdaPortTariffChargeDtlRepository chargeDtlRepository;
    private final PdaPortTariffSlabDtlRepository slabDtlRepository;
    private final ShipPortMasterRepository shipPortMasterRepository;
    private final ShipVesselTypeMasterRepository shipVesselTypeMasterRepository;
    private final ShipChargeMasterRepository shipChargeMasterRepository;
    private final PdaPortTariffMapper mapper;
    private final EntityManager entityManager;
    private final LoggingService loggingService;
    private final DocumentDeleteService documentDeleteService;
    private final DocumentSearchService documentSearchService;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAllTariffsWithFilters(String documentId, FilterRequestDto filterRequestDto, Pageable pageable, LocalDate periodFrom, LocalDate periodTo) {

        String operator = documentSearchService.resolveOperator(filterRequestDto);
        String isDeleted = documentSearchService.resolveIsDeleted(filterRequestDto);
        List<FilterDto> filters = documentSearchService.resolveDateFilters(filterRequestDto, "TRANSACTION_DATE", periodFrom, periodTo);

        RawSearchResult raw = documentSearchService.search(documentId, filters, operator, pageable, isDeleted,
                "DOC_REF",
                "TRANSACTION_POID");

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    @Override
    @Transactional(readOnly = true)
    public PdaPortTariffMasterResponse getTariffById(Long transactionPoid) {

        PdaPortTariffHdr tariff = tariffHdrRepository.findByTransactionPoid(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("PdaPortTariffHdr", "transactionPoid", transactionPoid));

        List<PdaPortTariffChargeDtl> chargeDetails = chargeDtlRepository.findByTransactionPoid(transactionPoid);

        for (PdaPortTariffChargeDtl chargeDetail : chargeDetails) {
            if (!entityManager.contains(chargeDetail)) {
                List<PdaPortTariffSlabDtl> slabDetails = slabDtlRepository.findByTransactionPoidAndChargeDetRowId(transactionPoid, chargeDetail.getId().getDetRowId());
                chargeDetail.setSlabDetails(slabDetails);
            }
        }

        return mapper.toResponseWithChargeDetails(tariff, chargeDetails);
    }

    @Override
    public PdaPortTariffMasterResponse createTariff(PdaPortTariffMasterRequest request) {
        validateCreateRequest(request, UserContext.getCompanyPoid());

        String portsStr = request.getPort();
        String vesselTypesStr = mapper.listToString(request.getVesselTypes());

        if (tariffHdrRepository.existsOverlappingPeriod(UserContext.getGroupPoid(), null, request.getPeriodFrom(), request.getPeriodTo(), portsStr, vesselTypesStr)) {
            throw new ValidationException("A tariff with overlapping period already exists for the selected port and vessel types.");
        }

        PdaPortTariffHdr tariffHdr = mapper.toEntity(request);
        PdaPortTariffHdr savedTariff = tariffHdrRepository.save(tariffHdr);

        if (request.getChargeDetails() != null && !request.getChargeDetails().isEmpty()) {
            saveChargeDetails(savedTariff, request.getChargeDetails());
        }

        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, UserContext.getDocumentId(), savedTariff.getTransactionPoid().toString());
        return getTariffById(savedTariff.getTransactionPoid());
    }

    @Override
    public PdaPortTariffMasterResponse updateTariff(Long transactionPoid, PdaPortTariffMasterRequest request) {
        validateUpdateRequest(request, UserContext.getGroupPoid());

        PdaPortTariffHdr existingTariff = tariffHdrRepository.findByTransactionPoid(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("PdaPortTariffHdr", "transactionPoid", transactionPoid));

        PdaPortTariffHdr oldTariff = new PdaPortTariffHdr();
        BeanUtils.copyProperties(existingTariff, oldTariff);

        String portsStr = request.getPort();
        String vesselTypesStr = mapper.listToString(request.getVesselTypes());

        if (tariffHdrRepository.existsOverlappingPeriod(UserContext.getGroupPoid(), transactionPoid, request.getPeriodFrom(), request.getPeriodTo(), portsStr, vesselTypesStr)) {
            throw new ValidationException("A tariff with overlapping period already exists for the selected port and vessel types.");
        }

        mapper.updateEntityFromRequest(existingTariff, request);
        tariffHdrRepository.save(existingTariff);

        if (request.getChargeDetails() != null && !request.getChargeDetails().isEmpty()) {
            updateChargeDetails(existingTariff, request.getChargeDetails());
        }

        entityManager.flush();
        entityManager.clear();

        loggingService.logChanges(oldTariff, existingTariff, PdaPortTariffHdr.class, UserContext.getDocumentId(), transactionPoid.toString(), LogDetailsEnum.MODIFIED, "TRANSACTION_POID");
        return getTariffById(transactionPoid);
    }

    @Override
    public void deleteTariff(Long transactionPoid, @Valid DeleteReasonDto deleteReasonDto) {
        PdaPortTariffHdr tariff = tariffHdrRepository.findByTransactionPoid(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("PdaPortTariffHdr", "transactionPoid", transactionPoid));

        documentDeleteService.deleteDocument(
                transactionPoid,
                "PDA_PORT_TARIFF_HDR",
                "TRANSACTION_POID",
                deleteReasonDto,
                tariff.getTransactionDate()
        );
    }

    @Override
    public PdaPortTariffMasterResponse copyTariff(Long sourceTransactionPoid, CopyTariffRequest request) {

        PdaPortTariffHdr sourceTariff = tariffHdrRepository.findByTransactionPoid(sourceTransactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("PdaPortTariffHdr", "transactionPoid", sourceTransactionPoid));

        PdaPortTariffMasterRequest copyRequest = mapper.toRequest(sourceTariff);
        copyRequest.setPeriodFrom(request.getNewPeriodFrom());
        copyRequest.setPeriodTo(request.getNewPeriodTo());

        return createTariff(copyRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public ChargeDetailsResponse getChargeDetails(Long transactionPoid, boolean includeSlabs) {

        tariffHdrRepository.findByTransactionPoid(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("PdaPortTariffHdr", "transactionPoid", transactionPoid));

        List<PdaPortTariffChargeDtl> chargeDetails = chargeDtlRepository.findByTransactionPoid(transactionPoid);

        if (includeSlabs) {
            for (PdaPortTariffChargeDtl chargeDetail : chargeDetails) {
                if (!entityManager.contains(chargeDetail)) {
                    List<PdaPortTariffSlabDtl> slabDetails = slabDtlRepository.findByTransactionPoidAndChargeDetRowId(transactionPoid, chargeDetail.getId().getDetRowId());
                    chargeDetail.setSlabDetails(slabDetails);
                }
            }
        }

        return mapper.toChargeDetailsResponse(chargeDetails, transactionPoid);
    }

    @Override
    public ChargeDetailsResponse bulkSaveChargeDetails(Long transactionPoid, ChargeDetailsRequest request) {

        PdaPortTariffHdr tariff = tariffHdrRepository.findByTransactionPoid(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("PdaPortTariffHdr", "transactionPoid", transactionPoid));

        if (request.getChargeDetails() != null && !request.getChargeDetails().isEmpty()) {
            updateChargeDetails(tariff, request.getChargeDetails());
        }

        entityManager.flush();
        entityManager.clear();

        return getChargeDetails(transactionPoid, true);
    }

    private void updateChargeDetails(PdaPortTariffHdr tariffHdr, List<PdaPortTariffChargeDetailRequest> chargeDetails) {
        for (PdaPortTariffChargeDetailRequest chargeRequest : chargeDetails) {
            ActionType action = chargeRequest.getActionType();

            if (action == null) {
                continue;
            }

            if (action == ActionType.isCreated) {
                createChargeDetail(tariffHdr, chargeRequest);
            } else if (action == ActionType.isUpdated) {
                PdaPortTariffChargeDtlId chargeId = new PdaPortTariffChargeDtlId();
                chargeId.setTransactionPoid(tariffHdr.getTransactionPoid());
                chargeId.setDetRowId(chargeRequest.getDetRowId());

                chargeDtlRepository.findById(chargeId).ifPresent(existing -> {
                    PdaPortTariffChargeDtl oldCharge = new PdaPortTariffChargeDtl();
                    BeanUtils.copyProperties(existing, oldCharge);
                    existing.setChargePoid(chargeRequest.getChargePoid() != null ? chargeRequest.getChargePoid().longValue() : null);
                    existing.setRateTypePoid(chargeRequest.getRateTypePoid() != null ? chargeRequest.getRateTypePoid().longValue() : null);
                    existing.setTariffSlab(chargeRequest.getTariffSlab());
                    existing.setFixRate(chargeRequest.getFixRate());
                    existing.setHarborCallType(chargeRequest.getHarborCallType());
                    existing.setIsEnabled(chargeRequest.getIsEnabled() != null ? chargeRequest.getIsEnabled() : "Y");
                    existing.setRemarks(chargeRequest.getRemarks());
                    existing.setSeqNo(chargeRequest.getSeqNo());
                    existing = chargeDtlRepository.save(existing);
                    String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getId().getTransactionPoid(), existing.getId().getDetRowId());
                    loggingService.createLog(oldCharge, existing, PdaPortTariffChargeDtl.class, UserContext.getDocumentId(), tariffHdr.getTransactionPoid().toString(), logDetail);

                    if (chargeRequest.getSlabDetails() != null) {
                        updateSlabDetails(tariffHdr.getTransactionPoid(), chargeRequest.getDetRowId(), chargeRequest.getSlabDetails());
                    }
                });
            } else if (action == ActionType.isDeleted) {
                PdaPortTariffChargeDtlId chargeId = new PdaPortTariffChargeDtlId();
                chargeId.setTransactionPoid(tariffHdr.getTransactionPoid());
                chargeId.setDetRowId(chargeRequest.getDetRowId());
                slabDtlRepository.deleteByTransactionPoidAndChargeDetRowId(tariffHdr.getTransactionPoid(), chargeRequest.getDetRowId());
                chargeDtlRepository.deleteById(chargeId);
                String logDetail = String.format("Row Deleted on [PDA Port Tariff Master Charge Details] with detRowId: %s", chargeRequest.getDetRowId());
                loggingService.createLogSummaryEntry(UserContext.getDocumentId(), tariffHdr.getTransactionPoid().toString(), logDetail);
            }
        }
    }

    private void updateSlabDetails(Long transactionPoid, Long chargeDetRowId, List<PdaPortTariffSlabDetailRequest> slabDetails) {
        for (PdaPortTariffSlabDetailRequest slabRequest : slabDetails) {
            ActionType action = slabRequest.getActionType();

            if (action == null) {
                continue;
            }

            if (action == ActionType.isCreated) {
                createSlabDetail(transactionPoid, chargeDetRowId, slabRequest);
            } else if (action == ActionType.isUpdated) {
                PdaPortTariffSlabDtlId slabId = new PdaPortTariffSlabDtlId();
                slabId.setTransactionPoid(transactionPoid);
                slabId.setChargeDetRowId(chargeDetRowId);
                slabId.setDetRowId(slabRequest.getDetRowId());

                slabDtlRepository.findById(slabId).ifPresent(existing -> {
                    PdaPortTariffSlabDtl oldSlab = new PdaPortTariffSlabDtl();
                    BeanUtils.copyProperties(existing, oldSlab);
                    existing.setQuantityFrom(slabRequest.getQuantityFrom());
                    existing.setQuantityTo(slabRequest.getQuantityTo());
                    existing.setDays1(slabRequest.getDays1());
                    existing.setRate1(slabRequest.getRate1());
                    existing.setDays2(slabRequest.getDays2());
                    existing.setRate2(slabRequest.getRate2());
                    existing.setDays3(slabRequest.getDays3());
                    existing.setRate3(slabRequest.getRate3());
                    existing.setDays4(slabRequest.getDays4());
                    existing.setRate4(slabRequest.getRate4());
                    existing.setCallByPort(slabRequest.getCallByPort());
                    existing.setRemarks(slabRequest.getRemarks());
                    existing = slabDtlRepository.save(existing);
                    String logDetail = String.format("KeyId = TRANSACTION_POID %s: CHARGE_DET_ROW_ID %s: DET_ROW_ID %s", existing.getId().getTransactionPoid(), existing.getId().getChargeDetRowId(), existing.getId().getDetRowId());
                    loggingService.createLog(oldSlab, existing, PdaPortTariffSlabDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                });
            } else if (action == ActionType.isDeleted) {
                PdaPortTariffSlabDtlId slabId = new PdaPortTariffSlabDtlId();
                slabId.setTransactionPoid(transactionPoid);
                slabId.setChargeDetRowId(chargeDetRowId);
                slabId.setDetRowId(slabRequest.getDetRowId());
                slabDtlRepository.deleteById(slabId);
                String logDetail = String.format("Row Deleted on [PDA Port Tariff Master Slab Details] with detRowId: %s", slabId.getDetRowId());
                loggingService.createLogSummaryEntry(UserContext.getDocumentId(), slabId.getTransactionPoid().toString(), logDetail);
            }
        }
    }

    private void createChargeDetail(PdaPortTariffHdr tariffHdr, PdaPortTariffChargeDetailRequest chargeRequest) {
        if (chargeRequest.getChargePoid() != null) {
            if (!shipChargeMasterRepository.existsByChargePoid(chargeRequest.getChargePoid())) {
                throw new ResourceNotFoundException("Charge Master", "Charge Poid", chargeRequest.getChargePoid());
            }
        }
        if (chargeRequest.getRateTypePoid() != null) {
            if (!pdaRateTypeMasterRepository.existsByRateTypePoid(chargeRequest.getRateTypePoid())) {
                throw new ResourceNotFoundException("Rate Type Master", "Rate Type Poid", chargeRequest.getRateTypePoid());
            }
        }

        PdaPortTariffChargeDtlId chargeId = new PdaPortTariffChargeDtlId();
        chargeId.setTransactionPoid(tariffHdr.getTransactionPoid());

        PdaPortTariffChargeDtl chargeDtl = new PdaPortTariffChargeDtl();
        chargeDtl.setId(chargeId);
        chargeDtl.setTariffHdr(tariffHdr);
        chargeDtl.setChargePoid(chargeRequest.getChargePoid() != null ? chargeRequest.getChargePoid().longValue() : null);
        chargeDtl.setRateTypePoid(chargeRequest.getRateTypePoid() != null ? chargeRequest.getRateTypePoid().longValue() : null);
        chargeDtl.setTariffSlab(chargeRequest.getTariffSlab());
        chargeDtl.setFixRate(chargeRequest.getFixRate());
        chargeDtl.setHarborCallType(chargeRequest.getHarborCallType());
        chargeDtl.setIsEnabled(chargeRequest.getIsEnabled() != null ? chargeRequest.getIsEnabled() : "Y");
        chargeDtl.setRemarks(chargeRequest.getRemarks());
        chargeDtl.setSeqNo(chargeRequest.getSeqNo());

        PdaPortTariffChargeDtl savedChargeDtl = chargeDtlRepository.save(chargeDtl);

        if (chargeRequest.getSlabDetails() != null && !chargeRequest.getSlabDetails().isEmpty()) {
            for (PdaPortTariffSlabDetailRequest slabRequest : chargeRequest.getSlabDetails()) {
                createSlabDetail(tariffHdr.getTransactionPoid(), savedChargeDtl.getId().getDetRowId(), slabRequest);
            }
        }
        String logDetail = String.format("Row Created on [PDA Port Tariff Master Charge Details] with detRowId: %s", savedChargeDtl.getId().getDetRowId());
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), tariffHdr.getTransactionPoid().toString(), logDetail);
    }

    private void createSlabDetail(Long transactionPoid, Long chargeDetRowId, PdaPortTariffSlabDetailRequest slabRequest) {
        PdaPortTariffSlabDtlId slabId = new PdaPortTariffSlabDtlId();
        slabId.setTransactionPoid(transactionPoid);
        slabId.setChargeDetRowId(chargeDetRowId);

        PdaPortTariffSlabDtl slabDtl = new PdaPortTariffSlabDtl();
        slabDtl.setId(slabId);
        slabDtl.setQuantityFrom(slabRequest.getQuantityFrom());
        slabDtl.setQuantityTo(slabRequest.getQuantityTo());
        slabDtl.setDays1(slabRequest.getDays1());
        slabDtl.setRate1(slabRequest.getRate1());
        slabDtl.setDays2(slabRequest.getDays2());
        slabDtl.setRate2(slabRequest.getRate2());
        slabDtl.setDays3(slabRequest.getDays3());
        slabDtl.setRate3(slabRequest.getRate3());
        slabDtl.setDays4(slabRequest.getDays4());
        slabDtl.setRate4(slabRequest.getRate4());
        slabDtl.setCallByPort(slabRequest.getCallByPort());
        slabDtl.setRemarks(slabRequest.getRemarks());

        slabDtlRepository.save(slabDtl);
        String logDetail = String.format("Row Created on [PDA Port Tariff Master Slab Details] with detRowId: %s", slabDtl.getId().getDetRowId());
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), slabId.getTransactionPoid().toString(), logDetail);
    }

    private void saveChargeDetails(PdaPortTariffHdr tariffHdr, List<PdaPortTariffChargeDetailRequest> chargeDetails) {
        int seqNo = 1;
        for (PdaPortTariffChargeDetailRequest chargeRequest : chargeDetails) {
            if (chargeRequest.getChargePoid() != null) {
                if (!shipChargeMasterRepository.existsByChargePoid(chargeRequest.getChargePoid())) {
                    throw new ResourceNotFoundException("Charge Master", "Charge Poid", chargeRequest.getChargePoid());
                }
            }
            if (chargeRequest.getRateTypePoid() != null) {
                if (!pdaRateTypeMasterRepository.existsByRateTypePoid(chargeRequest.getRateTypePoid())) {
                    throw new ResourceNotFoundException("Rate Type Master", "Rate Type Poid", chargeRequest.getRateTypePoid());
                }
            }

            if (chargeRequest.getSeqNo() == null) {
                chargeRequest.setSeqNo(seqNo++);
            } else {
                seqNo = chargeRequest.getSeqNo() + 1;
            }

            PdaPortTariffChargeDtlId chargeId = new PdaPortTariffChargeDtlId();
            chargeId.setTransactionPoid(tariffHdr.getTransactionPoid());

            PdaPortTariffChargeDtl chargeDtl = new PdaPortTariffChargeDtl();
            chargeDtl.setId(chargeId);
            chargeDtl.setTariffHdr(tariffHdr);
            chargeDtl.setChargePoid(chargeRequest.getChargePoid() != null ? chargeRequest.getChargePoid().longValue() : null);
//            chargeDtl(chargeRequest.getChargePoid());


            chargeDtl.setRateTypePoid(chargeRequest.getRateTypePoid() != null ? chargeRequest.getRateTypePoid().longValue() : null);
            chargeDtl.setTariffSlab(chargeRequest.getTariffSlab());
            chargeDtl.setFixRate(chargeRequest.getFixRate());
            chargeDtl.setHarborCallType(chargeRequest.getHarborCallType());
            chargeDtl.setIsEnabled(chargeRequest.getIsEnabled() != null ? chargeRequest.getIsEnabled() : "Y");
            chargeDtl.setRemarks(chargeRequest.getRemarks());
            chargeDtl.setSeqNo(chargeRequest.getSeqNo());

            PdaPortTariffChargeDtl savedChargeDtl = chargeDtlRepository.save(chargeDtl);

            if (chargeRequest.getSlabDetails() != null && !chargeRequest.getSlabDetails().isEmpty()) {
                for (PdaPortTariffSlabDetailRequest slabRequest : chargeRequest.getSlabDetails()) {
                    PdaPortTariffSlabDtlId slabId = new PdaPortTariffSlabDtlId();
                    slabId.setTransactionPoid(tariffHdr.getTransactionPoid());
                    slabId.setChargeDetRowId(savedChargeDtl.getId().getDetRowId());

                    PdaPortTariffSlabDtl slabDtl = new PdaPortTariffSlabDtl();
                    slabDtl.setId(slabId);
                    slabDtl.setChargeDtl(savedChargeDtl);
                    slabDtl.setQuantityFrom(slabRequest.getQuantityFrom());
                    slabDtl.setQuantityTo(slabRequest.getQuantityTo());
                    slabDtl.setDays1(slabRequest.getDays1());
                    slabDtl.setRate1(slabRequest.getRate1());
                    slabDtl.setDays2(slabRequest.getDays2());
                    slabDtl.setRate2(slabRequest.getRate2());
                    slabDtl.setDays3(slabRequest.getDays3());
                    slabDtl.setRate3(slabRequest.getRate3());
                    slabDtl.setDays4(slabRequest.getDays4());
                    slabDtl.setRate4(slabRequest.getRate4());
                    slabDtl.setCallByPort(slabRequest.getCallByPort());
                    slabDtl.setRemarks(slabRequest.getRemarks());

                    slabDtlRepository.save(slabDtl);
                }
            }
        }
    }

    private void validateCreateRequest(PdaPortTariffMasterRequest request, Long groupPoid) {
        if (StringUtils.isNotBlank(request.getPort())) {
            if (!shipPortMasterRepository.existsByIdPortPoidAndIdGroupPoid(BigDecimal.valueOf(Long.parseLong(request.getPort())), BigDecimal.valueOf(groupPoid))) {
                throw new ResourceNotFoundException("Port", "Port Poid", request.getPort());
            }
        }
        for (String vesselPoid : request.getVesselTypes()) {
            if (StringUtils.isNotBlank(vesselPoid)) {
                if (!shipVesselTypeMasterRepository.existsByVesselTypePoidAndGroupPoid(BigDecimal.valueOf(Long.parseLong(vesselPoid)), BigDecimal.valueOf(groupPoid))) {
                    throw new ResourceNotFoundException("Vessel", "Vessel Poid", vesselPoid);
                }
            }
        }
    }

    private void validateUpdateRequest(PdaPortTariffMasterRequest request, Long groupPoid) {
        if (!shipPortMasterRepository.existsByIdPortPoidAndIdGroupPoid(BigDecimal.valueOf(Long.parseLong(request.getPort())), BigDecimal.valueOf(groupPoid))) {
            throw new ResourceNotFoundException("Port", "Port Poid", request.getPort());
        }
        for (String vesselPoid : request.getVesselTypes()) {
            if (!shipVesselTypeMasterRepository.existsByVesselTypePoidAndGroupPoid(BigDecimal.valueOf(Long.parseLong(vesselPoid)), BigDecimal.valueOf(groupPoid))) {
                throw new ResourceNotFoundException("Vessel", "Vessel Poid", vesselPoid);
            }
        }
    }
}