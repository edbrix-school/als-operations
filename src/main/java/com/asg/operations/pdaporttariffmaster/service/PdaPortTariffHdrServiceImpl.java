package com.asg.operations.pdaporttariffmaster.service;

import com.asg.operations.pdaporttariffmaster.dto.*;
import com.asg.operations.pdaporttariffmaster.entity.PdaPortTariffChargeDtl;
import com.asg.operations.pdaporttariffmaster.entity.PdaPortTariffHdr;
import com.asg.operations.pdaporttariffmaster.entity.PdaPortTariffSlabDtl;
import com.asg.operations.pdaporttariffmaster.key.PdaPortTariffChargeDtlId;
import com.asg.operations.pdaporttariffmaster.key.PdaPortTariffSlabDtlId;
import com.asg.operations.pdaporttariffmaster.repository.*;
import com.asg.operations.portcallreport.enums.ActionType;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.pdaporttariffmaster.util.DateOverlapValidator;
import com.asg.operations.pdaporttariffmaster.util.PdaPortTariffMapper;
import com.asg.operations.pdaporttariffmaster.util.PortTariffDocumentRefGenerator;
import jakarta.persistence.EntityManager;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private final DateOverlapValidator overlapValidator;
    private final PortTariffDocumentRefGenerator docRefGenerator;
    private final EntityManager entityManager;


    @Override
    @Transactional(readOnly = true)
    public PageResponse<PdaPortTariffMasterResponse> getTariffList(
            String portPoid,
            LocalDate periodFrom,
            LocalDate periodTo,
            String vesselTypePoid,
            Long groupPoid,
            Pageable pageable
    ) {
        BigDecimal groupPoidBD = BigDecimal.valueOf(groupPoid);

        Page<PdaPortTariffHdr> tariffPage = tariffHdrRepository.searchTariffs(
                groupPoidBD,
                portPoid,
                periodFrom,
                periodTo,
                vesselTypePoid,
                pageable
        );

        List<PdaPortTariffMasterResponse> content = tariffPage.getContent().stream()
                .map(mapper::toHeaderOnlyResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                tariffPage.getNumber(),
                tariffPage.getSize(),
                tariffPage.getTotalElements(),
                tariffPage.getTotalPages(),
                tariffPage.isFirst(),
                tariffPage.isLast(),
                tariffPage.getNumberOfElements()
        );
    }


    @Override
    @Transactional(readOnly = true)
    public PdaPortTariffMasterResponse getTariffById(Long transactionPoid, Long groupPoid) {
        BigDecimal groupPoidBD = BigDecimal.valueOf(groupPoid);

        PdaPortTariffHdr tariff = tariffHdrRepository.findByTransactionPoidAndGroupPoid(
                        transactionPoid, groupPoidBD)
                .orElseThrow(() -> new ResourceNotFoundException("PdaPortTariffHdr", "transactionPoid", transactionPoid));

        List<PdaPortTariffChargeDtl> chargeDetails = chargeDtlRepository.findByTransactionPoidOrderBySeqNoAscDetRowIdAsc(transactionPoid);

        for (PdaPortTariffChargeDtl chargeDetail : chargeDetails) {
            if (!entityManager.contains(chargeDetail)) {
                List<PdaPortTariffSlabDtl> slabDetails = slabDtlRepository.findByTransactionPoidAndChargeDetRowIdOrderByDetRowIdAsc(
                        transactionPoid, chargeDetail.getId().getDetRowId());
                chargeDetail.setSlabDetails(slabDetails);
            }
        }

        return mapper.toResponseWithChargeDetails(tariff, chargeDetails);
    }

    @Override
    public PdaPortTariffMasterResponse createTariff(PdaPortTariffMasterRequest request, Long groupPoid, Long companyPoid, String userId) {
        validateCreateRequest(request, groupPoid);

        BigDecimal groupPoidBD = BigDecimal.valueOf(groupPoid);
        BigDecimal companyPoidBD = BigDecimal.valueOf(companyPoid);

        String docRef = docRefGenerator.generateDocRef(groupPoidBD);

        String portsStr = mapper.listToString(request.getPorts());
        String vesselTypesStr = mapper.listToString(request.getVesselTypes());

        if (tariffHdrRepository.existsOverlappingPeriod(
                groupPoidBD, null, request.getPeriodFrom(), request.getPeriodTo(), portsStr, vesselTypesStr)) {
            throw new ValidationException("A tariff with overlapping period already exists for the selected ports and vessel types.");
        }

        PdaPortTariffHdr tariffHdr = mapper.toEntity(request, groupPoidBD, companyPoidBD, docRef, userId);
        PdaPortTariffHdr savedTariff = tariffHdrRepository.save(tariffHdr);

        if (request.getChargeDetails() != null && !request.getChargeDetails().isEmpty()) {
            saveChargeDetails(savedTariff, request.getChargeDetails(), userId);
        }

        return getTariffById(savedTariff.getTransactionPoid(), groupPoid);
    }

    @Override
    public PdaPortTariffMasterResponse updateTariff(Long transactionPoid, PdaPortTariffMasterRequest request, Long groupPoid, String userId) {
        validateUpdateRequest(request, groupPoid);

        BigDecimal groupPoidBD = BigDecimal.valueOf(groupPoid);

        PdaPortTariffHdr existingTariff = tariffHdrRepository.findByTransactionPoidAndGroupPoidAndDeleted(
                        transactionPoid, groupPoidBD, "N")
                .orElseThrow(() -> new ResourceNotFoundException("PdaPortTariffHdr", "transactionPoid", transactionPoid));

        String portsStr = mapper.listToString(request.getPorts());
        String vesselTypesStr = mapper.listToString(request.getVesselTypes());

        if (tariffHdrRepository.existsOverlappingPeriod(
                groupPoidBD, transactionPoid, request.getPeriodFrom(), request.getPeriodTo(), portsStr, vesselTypesStr)) {
            throw new ValidationException("A tariff with overlapping period already exists for the selected ports and vessel types.");
        }

        mapper.updateEntityFromRequest(existingTariff, request, userId);
        tariffHdrRepository.save(existingTariff);

        if (request.getChargeDetails() != null && !request.getChargeDetails().isEmpty()) {
            updateChargeDetails(existingTariff, request.getChargeDetails(), userId);
        }

        entityManager.flush();
        entityManager.clear();

        return getTariffById(transactionPoid, groupPoid);
    }

    @Override
    public void deleteTariff(Long transactionPoid, Long groupPoid, String userId, boolean hardDelete) {
        BigDecimal groupPoidBD = BigDecimal.valueOf(groupPoid);

        PdaPortTariffHdr tariff = tariffHdrRepository.findByTransactionPoidAndGroupPoidAndDeleted(
                        transactionPoid, groupPoidBD, "N")
                .orElseThrow(() -> new ResourceNotFoundException("PdaPortTariffHdr", "transactionPoid", transactionPoid));

        if (hardDelete) {
            slabDtlRepository.deleteByTransactionPoid(transactionPoid);
            chargeDtlRepository.deleteByTransactionPoid(transactionPoid);
            tariffHdrRepository.delete(tariff);
        } else {
            tariff.setDeleted("Y");
            tariff.setLastModifiedBy(userId);
            tariff.setLastModifiedDate(LocalDateTime.now());
            tariffHdrRepository.save(tariff);
        }
    }

    @Override
    public PdaPortTariffMasterResponse copyTariff(Long sourceTransactionPoid, CopyTariffRequest request, Long groupPoid, String userId) {
        BigDecimal groupPoidBD = BigDecimal.valueOf(groupPoid);

        PdaPortTariffHdr sourceTariff = tariffHdrRepository.findByTransactionPoidAndGroupPoidAndDeleted(
                        sourceTransactionPoid, groupPoidBD, "N")
                .orElseThrow(() -> new ResourceNotFoundException("PdaPortTariffHdr", "transactionPoid", sourceTransactionPoid));

        PdaPortTariffMasterRequest copyRequest = mapper.toRequest(sourceTariff);
        copyRequest.setPeriodFrom(request.getNewPeriodFrom());
        copyRequest.setPeriodTo(request.getNewPeriodTo());

        return createTariff(copyRequest, groupPoid, sourceTariff.getCompanyPoid().longValue(), userId);
    }

    @Override
    @Transactional(readOnly = true)
    public ChargeDetailsResponse getChargeDetails(Long transactionPoid, Long groupPoid, boolean includeSlabs) {
        BigDecimal groupPoidBD = BigDecimal.valueOf(groupPoid);

        PdaPortTariffHdr tariff = tariffHdrRepository.findByTransactionPoidAndGroupPoidAndDeleted(
                        transactionPoid, groupPoidBD, "N")
                .orElseThrow(() -> new ResourceNotFoundException("PdaPortTariffHdr", "transactionPoid", transactionPoid));

        List<PdaPortTariffChargeDtl> chargeDetails = chargeDtlRepository.findByTransactionPoidOrderBySeqNoAscDetRowIdAsc(transactionPoid);

        if (includeSlabs) {
            for (PdaPortTariffChargeDtl chargeDetail : chargeDetails) {
                if (!entityManager.contains(chargeDetail)) {
                    List<PdaPortTariffSlabDtl> slabDetails = slabDtlRepository.findByTransactionPoidAndChargeDetRowIdOrderByDetRowIdAsc(
                            transactionPoid, chargeDetail.getId().getDetRowId());
                    chargeDetail.setSlabDetails(slabDetails);
                }
            }
        }

        return mapper.toChargeDetailsResponse(chargeDetails, transactionPoid);
    }

    @Override
    public ChargeDetailsResponse bulkSaveChargeDetails(Long transactionPoid, ChargeDetailsRequest request, Long groupPoid, String userId) {
        BigDecimal groupPoidBD = BigDecimal.valueOf(groupPoid);

        PdaPortTariffHdr tariff = tariffHdrRepository.findByTransactionPoidAndGroupPoidAndDeleted(
                        transactionPoid, groupPoidBD, "N")
                .orElseThrow(() -> new ResourceNotFoundException("PdaPortTariffHdr", "transactionPoid", transactionPoid));

        if (request.getChargeDetails() != null && !request.getChargeDetails().isEmpty()) {
            updateChargeDetails(tariff, request.getChargeDetails(), userId);
        }

        entityManager.flush();
        entityManager.clear();

        return getChargeDetails(transactionPoid, groupPoid, true);
    }

    private void updateChargeDetails(PdaPortTariffHdr tariffHdr, List<PdaPortTariffChargeDetailRequest> chargeDetails, String currentUser) {
        for (PdaPortTariffChargeDetailRequest chargeRequest : chargeDetails) {
            ActionType action = chargeRequest.getActionType();
            
            if (action == null) {
                continue;
            }

            if (action == ActionType.isCreated) {
                createChargeDetail(tariffHdr, chargeRequest, currentUser);
            } else if (action == ActionType.isUpdated) {
                PdaPortTariffChargeDtlId chargeId = new PdaPortTariffChargeDtlId();
                chargeId.setTransactionPoid(tariffHdr.getTransactionPoid());
                chargeId.setDetRowId(chargeRequest.getDetRowId());

                chargeDtlRepository.findById(chargeId).ifPresent(existing -> {
                    existing.setChargePoid(chargeRequest.getChargePoid().longValue());
                    existing.setRateTypePoid(chargeRequest.getRateTypePoid().longValue());
                    existing.setTariffSlab(chargeRequest.getTariffSlab());
                    existing.setFixRate(chargeRequest.getFixRate());
                    existing.setHarborCallType(chargeRequest.getHarborCallType());
                    existing.setIsEnabled(chargeRequest.getIsEnabled() != null ? chargeRequest.getIsEnabled() : "Y");
                    existing.setRemarks(chargeRequest.getRemarks());
                    existing.setSeqNo(chargeRequest.getSeqNo());
                    existing.setLastModifiedBy(currentUser);
                    existing.setLastModifiedDate(LocalDateTime.now());
                    chargeDtlRepository.save(existing);
                    
                    if (chargeRequest.getSlabDetails() != null) {
                        updateSlabDetails(tariffHdr.getTransactionPoid(), chargeRequest.getDetRowId(), chargeRequest.getSlabDetails(), currentUser);
                    }
                });
            } else if (action == ActionType.isDeleted) {
                PdaPortTariffChargeDtlId chargeId = new PdaPortTariffChargeDtlId();
                chargeId.setTransactionPoid(tariffHdr.getTransactionPoid());
                chargeId.setDetRowId(chargeRequest.getDetRowId());
                slabDtlRepository.deleteByTransactionPoidAndChargeDetRowId(tariffHdr.getTransactionPoid(), chargeRequest.getDetRowId());
                chargeDtlRepository.deleteById(chargeId);
            }
        }
    }

    private void updateSlabDetails(Long transactionPoid, Long chargeDetRowId, List<PdaPortTariffSlabDetailRequest> slabDetails, String currentUser) {
        for (PdaPortTariffSlabDetailRequest slabRequest : slabDetails) {
            ActionType action = slabRequest.getActionType();
            
            if (action == null) {
                continue;
            }

            if (action == ActionType.isCreated) {
                createSlabDetail(transactionPoid, chargeDetRowId, slabRequest, currentUser);
            } else if (action == ActionType.isUpdated) {
                PdaPortTariffSlabDtlId slabId = new PdaPortTariffSlabDtlId();
                slabId.setTransactionPoid(transactionPoid);
                slabId.setChargeDetRowId(chargeDetRowId);
                slabId.setDetRowId(slabRequest.getDetRowId());

                slabDtlRepository.findById(slabId).ifPresent(existing -> {
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
                    existing.setLastModifiedBy(currentUser);
                    existing.setLastModifiedDate(LocalDateTime.now());
                    slabDtlRepository.save(existing);
                });
            } else if (action == ActionType.isDeleted) {
                PdaPortTariffSlabDtlId slabId = new PdaPortTariffSlabDtlId();
                slabId.setTransactionPoid(transactionPoid);
                slabId.setChargeDetRowId(chargeDetRowId);
                slabId.setDetRowId(slabRequest.getDetRowId());
                slabDtlRepository.deleteById(slabId);
            }
        }
    }

    private void createChargeDetail(PdaPortTariffHdr tariffHdr, PdaPortTariffChargeDetailRequest chargeRequest, String currentUser) {
        if (!shipChargeMasterRepository.existsByChargePoidAndActiveIgnoreCaseAndDeletedIgnoreCase(chargeRequest.getChargePoid(), "Y", "N")) {
            throw new ResourceNotFoundException("Charge Master", "Charge Poid", chargeRequest.getChargePoid());
        }
        if (!pdaRateTypeMasterRepository.existsByRateTypePoidAndDeletedIgnoreCase(chargeRequest.getRateTypePoid(), "N")) {
            throw new ResourceNotFoundException("Rate Type Master", "Rate Type Poid", chargeRequest.getRateTypePoid());
        }

        PdaPortTariffChargeDtlId chargeId = new PdaPortTariffChargeDtlId();
        chargeId.setTransactionPoid(tariffHdr.getTransactionPoid());

        PdaPortTariffChargeDtl chargeDtl = new PdaPortTariffChargeDtl();
        chargeDtl.setId(chargeId);
        chargeDtl.setTariffHdr(tariffHdr);
        chargeDtl.setChargePoid(chargeRequest.getChargePoid().longValue());
        chargeDtl.setRateTypePoid(chargeRequest.getRateTypePoid().longValue());
        chargeDtl.setTariffSlab(chargeRequest.getTariffSlab());
        chargeDtl.setFixRate(chargeRequest.getFixRate());
        chargeDtl.setHarborCallType(chargeRequest.getHarborCallType());
        chargeDtl.setIsEnabled(chargeRequest.getIsEnabled() != null ? chargeRequest.getIsEnabled() : "Y");
        chargeDtl.setRemarks(chargeRequest.getRemarks());
        chargeDtl.setSeqNo(chargeRequest.getSeqNo());
        chargeDtl.setCreatedBy(currentUser);
        chargeDtl.setCreatedDate(LocalDateTime.now());
        chargeDtl.setLastModifiedBy(currentUser);
        chargeDtl.setLastModifiedDate(LocalDateTime.now());

        PdaPortTariffChargeDtl savedChargeDtl = chargeDtlRepository.save(chargeDtl);

        if (chargeRequest.getSlabDetails() != null && !chargeRequest.getSlabDetails().isEmpty()) {
            for (PdaPortTariffSlabDetailRequest slabRequest : chargeRequest.getSlabDetails()) {
                createSlabDetail(tariffHdr.getTransactionPoid(), savedChargeDtl.getId().getDetRowId(), slabRequest, currentUser);
            }
        }
    }

    private void createSlabDetail(Long transactionPoid, Long chargeDetRowId, PdaPortTariffSlabDetailRequest slabRequest, String currentUser) {
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
        slabDtl.setCreatedBy(currentUser);
        slabDtl.setCreatedDate(LocalDateTime.now());
        slabDtl.setLastModifiedBy(currentUser);
        slabDtl.setLastModifiedDate(LocalDateTime.now());

        slabDtlRepository.save(slabDtl);
    }

    private void saveChargeDetails(PdaPortTariffHdr tariffHdr, List<PdaPortTariffChargeDetailRequest> chargeDetails, String currentUser) {
        int seqNo = 1;
        for (PdaPortTariffChargeDetailRequest chargeRequest : chargeDetails) {

            if (!shipChargeMasterRepository.existsByChargePoidAndActiveIgnoreCaseAndDeletedIgnoreCase(chargeRequest.getChargePoid(), "Y", "N")) {
                throw new ResourceNotFoundException("Charge Master", "Charge Poid", chargeRequest.getChargePoid());
            }
            if (!pdaRateTypeMasterRepository.existsByRateTypePoidAndDeletedIgnoreCase(chargeRequest.getRateTypePoid(), "N")) {
                throw new ResourceNotFoundException("Rate Type Master", "Rate Type Poid", chargeRequest.getRateTypePoid());
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
            chargeDtl.setChargePoid(chargeRequest.getChargePoid().longValue());
//            chargeDtl(chargeRequest.getChargePoid());


            chargeDtl.setRateTypePoid(chargeRequest.getRateTypePoid().longValue());
            chargeDtl.setTariffSlab(chargeRequest.getTariffSlab());
            chargeDtl.setFixRate(chargeRequest.getFixRate());
            chargeDtl.setHarborCallType(chargeRequest.getHarborCallType());
            chargeDtl.setIsEnabled(chargeRequest.getIsEnabled() != null ? chargeRequest.getIsEnabled() : "Y");
            chargeDtl.setRemarks(chargeRequest.getRemarks());
            chargeDtl.setSeqNo(chargeRequest.getSeqNo());
            chargeDtl.setCreatedBy(currentUser);
            chargeDtl.setCreatedDate(LocalDateTime.now());
            chargeDtl.setLastModifiedBy(currentUser);
            chargeDtl.setLastModifiedDate(LocalDateTime.now());

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
                    slabDtl.setCreatedBy(currentUser);
                    slabDtl.setCreatedDate(LocalDateTime.now());
                    slabDtl.setLastModifiedBy(currentUser);
                    slabDtl.setLastModifiedDate(LocalDateTime.now());

                    slabDtlRepository.save(slabDtl);
                }
            }
        }
    }

    private void validateCreateRequest(PdaPortTariffMasterRequest request, Long groupPoid) {
        if (request.getPorts() == null || request.getPorts().isEmpty()) {
            throw new ValidationException("Ports cannot be empty");
        }
        for (String portPoid : request.getPorts()) {
            if (!shipPortMasterRepository.existsByIdPortPoidAndIdGroupPoid(BigDecimal.valueOf(Long.parseLong(portPoid)), BigDecimal.valueOf(groupPoid))) {
                throw new ResourceNotFoundException("Port", "Port Poid", portPoid);
            }
        }
        for (String vesselPoid : request.getVesselTypes()) {
            if (!shipVesselTypeMasterRepository.existsByVesselTypePoidAndGroupPoid(BigDecimal.valueOf(Long.parseLong(vesselPoid)), BigDecimal.valueOf(groupPoid))) {
                throw new ResourceNotFoundException("Vessel", "Vessel Poid", vesselPoid);
            }
        }
    }

    private void validateUpdateRequest(PdaPortTariffMasterRequest request, Long groupPoid) {
        if (request.getPorts() == null || request.getPorts().isEmpty()) {
            throw new ValidationException("Ports cannot be empty");
        }
        for (String portPoid : request.getPorts()) {
            if (!shipPortMasterRepository.existsByIdPortPoidAndIdGroupPoid(BigDecimal.valueOf(Long.parseLong(portPoid)), BigDecimal.valueOf(groupPoid))) {
                throw new ResourceNotFoundException("Port", "Port Poid", portPoid);
            }
        }
        for (String vesselPoid : request.getVesselTypes()) {
            if (!shipVesselTypeMasterRepository.existsByVesselTypePoidAndGroupPoid(BigDecimal.valueOf(Long.parseLong(vesselPoid)), BigDecimal.valueOf(groupPoid))) {
                throw new ResourceNotFoundException("Vessel", "Vessel Poid", vesselPoid);
            }
        }
    }
}