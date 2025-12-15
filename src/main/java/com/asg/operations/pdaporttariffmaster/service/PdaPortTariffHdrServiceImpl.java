package com.asg.operations.pdaporttariffmaster.service;

import com.asg.common.lib.security.util.UserContext;
import com.asg.operations.commonlov.dto.LovItem;
import com.asg.operations.commonlov.service.LovService;
import com.asg.operations.pdaentryform.dto.PdaEntryResponse;
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
import jakarta.persistence.Query;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
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
    private final LovService lovService;

    @Override
    @Transactional(readOnly = true)
    public Page<PdaPortTariffListResponse> getAllTariffsWithFilters(
            Long groupPoid, Long companyPoid,
            GetAllTariffFilterRequest filterRequest,
            int page, int size, String sort) {

        // Build dynamic SQL query
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT t.TRANSACTION_POID, t.DOC_REF, t.TRANSACTION_DATE, t.PORTS, ");
        sqlBuilder.append("t.VESSEL_TYPES, t.PERIOD_FROM, t.PERIOD_TO, t.REMARKS, t.DELETED, ");
        sqlBuilder.append("t.CREATED_DATE, t.LASTMODIFIED_DATE ");
        sqlBuilder.append("FROM PDA_PORT_TARIFF_HDR t ");
        sqlBuilder.append("WHERE t.GROUP_POID = :groupPoid AND t.COMPANY_POID = :companyPoid ");

        // Apply isDeleted filter
        if (filterRequest.getIsDeleted() != null && "N".equalsIgnoreCase(filterRequest.getIsDeleted())) {
            sqlBuilder.append("AND (t.DELETED IS NULL OR t.DELETED != 'Y') ");
        } else if (filterRequest.getIsDeleted() != null && "Y".equalsIgnoreCase(filterRequest.getIsDeleted())) {
            sqlBuilder.append("AND t.DELETED = 'Y' ");
        }

        // Apply date range filters
        if (StringUtils.hasText(filterRequest.getFrom())) {
            sqlBuilder.append("AND TRUNC(t.TRANSACTION_DATE) >= TO_DATE(:fromDate, 'YYYY-MM-DD') ");
        }
        if (StringUtils.hasText(filterRequest.getTo())) {
            sqlBuilder.append("AND TRUNC(t.TRANSACTION_DATE) <= TO_DATE(:toDate, 'YYYY-MM-DD') ");
        }

        // Build filter conditions with sequential parameter indexing
        List<String> filterConditions = new java.util.ArrayList<>();
        List<GetAllTariffFilterRequest.FilterItem> validFilters = new java.util.ArrayList<>();
        if (filterRequest.getFilters() != null && !filterRequest.getFilters().isEmpty()) {
            for (GetAllTariffFilterRequest.FilterItem filter : filterRequest.getFilters()) {
                if (StringUtils.hasText(filter.getSearchField()) && StringUtils.hasText(filter.getSearchValue())) {
                    validFilters.add(filter);
                    String columnName = mapTariffSearchFieldToColumn(filter.getSearchField());
                    int paramIndex = validFilters.size() - 1;
                    filterConditions.add("LOWER(" + columnName + ") LIKE LOWER(:filterValue" + paramIndex + ")");
                }
            }
        }

        // Add filter conditions with operator
        if (!filterConditions.isEmpty()) {
            String operator = "AND".equalsIgnoreCase(filterRequest.getOperator()) ? " AND " : " OR ";
            sqlBuilder.append("AND (").append(String.join(operator, filterConditions)).append(") ");
        }

        // Apply sorting
        String orderBy = "ORDER BY t.TRANSACTION_DATE DESC";
        if (StringUtils.hasText(sort)) {
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                String sortField = mapTariffSortFieldToColumn(sortParts[0].trim());
                String sortDirection = sortParts[1].trim().toUpperCase();
                if ("ASC".equals(sortDirection) || "DESC".equals(sortDirection)) {
                    orderBy = "ORDER BY " + sortField + " " + sortDirection + " NULLS LAST";
                }
            }
        }
        sqlBuilder.append(orderBy);

        // Create count query
        String countSql = "SELECT COUNT(*) FROM (" + sqlBuilder.toString() + ")";

        // Create query
        Query query = entityManager.createNativeQuery(sqlBuilder.toString());
        Query countQuery = entityManager.createNativeQuery(countSql);

        // Set parameters
        query.setParameter("groupPoid", groupPoid);
        query.setParameter("companyPoid", companyPoid);
        countQuery.setParameter("groupPoid", groupPoid);
        countQuery.setParameter("companyPoid", companyPoid);

        if (StringUtils.hasText(filterRequest.getFrom())) {
            query.setParameter("fromDate", filterRequest.getFrom());
            countQuery.setParameter("fromDate", filterRequest.getFrom());
        }
        if (StringUtils.hasText(filterRequest.getTo())) {
            query.setParameter("toDate", filterRequest.getTo());
            countQuery.setParameter("toDate", filterRequest.getTo());
        }

        // Set filter parameters using sequential indexing
        if (!validFilters.isEmpty()) {
            for (int i = 0; i < validFilters.size(); i++) {
                GetAllTariffFilterRequest.FilterItem filter = validFilters.get(i);
                String paramValue = "%" + filter.getSearchValue() + "%";
                query.setParameter("filterValue" + i, paramValue);
                countQuery.setParameter("filterValue" + i, paramValue);
            }
        }

        // Get total count
        Long totalCount = ((Number) countQuery.getSingleResult()).longValue();

        // Apply pagination
        int offset = page * size;
        query.setFirstResult(offset);
        query.setMaxResults(size);

        // Execute query and map results
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        List<PdaPortTariffListResponse> dtos = results.stream()
                .map(this::mapToTariffListResponseDto)
                .collect(Collectors.toList());

        // Create page
        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(dtos, pageable, totalCount);
    }

    private String mapTariffSearchFieldToColumn(String searchField) {
        if (searchField == null) {
            return null;
        }
        // Normalize the field name by removing underscores and converting to uppercase
        String normalizedField = searchField.toUpperCase().replace("_", "");

        switch (normalizedField) {
            case "DOCREF":
                return "t.DOC_REF";
            case "PORTS":
            case "PORT":
                return "t.PORTS";
            case "VESSELTYPES":
            case "VESSELTYPE":
            case "VESSEL":
                return "t.VESSEL_TYPES";
            case "REMARKS":
                return "t.REMARKS";
            case "PERIODFROM":
                return "t.PERIOD_FROM";
            case "PERIODTO":
                return "t.PERIOD_TO";
            default:
                // Fallback: assume it's a direct column name from t table
                String columnName = searchField.toUpperCase().replace(" ", "_");
                return "t." + columnName;
        }
    }

    private String mapTariffSortFieldToColumn(String sortField) {
        if (sortField == null) {
            return "t.TRANSACTION_DATE";
        }
        String normalizedField = sortField.toUpperCase().replace("_", "");

        switch (normalizedField) {
            case "TRANSACTIONPOID":
                return "t.TRANSACTION_POID";
            case "DOCREF":
                return "t.DOC_REF";
            case "TRANSACTIONDATE":
                return "t.TRANSACTION_DATE";
            case "PORTS":
            case "PORT":
                return "t.PORTS";
            case "VESSELTYPES":
            case "VESSELTYPE":
            case "VESSEL":
                return "t.VESSEL_TYPES";
            case "PERIODFROM":
                return "t.PERIOD_FROM";
            case "PERIODTO":
                return "t.PERIOD_TO";
            case "REMARKS":
                return "t.REMARKS";
            case "DELETED":
                return "t.DELETED";
            case "CREATEDDATE":
                return "t.CREATED_DATE";
            case "LASTMODIFIEDDATE":
                return "t.LASTMODIFIED_DATE";
            default:
                String columnName = sortField.toUpperCase().replace(" ", "_");
                return "t." + columnName;
        }
    }

    private PdaPortTariffListResponse mapToTariffListResponseDto(Object[] row) {
        PdaPortTariffListResponse dto = new PdaPortTariffListResponse();

        dto.setTransactionPoid(row[0] != null ? ((Number) row[0]).longValue() : null);
        dto.setDocRef(convertToString(row[1]));
        dto.setTransactionDate(row[2] != null ? ((Timestamp) row[2]).toLocalDateTime().toLocalDate() : null);
        dto.setPort(convertToString(row[3]));
        dto.setPeriodFrom(row[5] != null ? ((Timestamp) row[5]).toLocalDateTime().toLocalDate() : null);
        dto.setPeriodTo(row[6] != null ? ((Timestamp) row[6]).toLocalDateTime().toLocalDate() : null);
        dto.setRemarks(convertToString(row[7]));
        dto.setDeleted(convertToString(row[8]));
        dto.setCreatedDate(row[9] != null ? ((Timestamp) row[9]).toLocalDateTime() : null);
        dto.setLastModifiedDate(row[10] != null ? ((Timestamp) row[10]).toLocalDateTime() : null);
        LovItem lovItem = dto.getPort() != null ? lovService.getLovItemByPoid(Long.parseLong(dto.getPort()), "PDA_PORT_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()) : null;
        dto.setPortName(lovItem != null ? lovItem.getLabel() : null);
        return dto;
    }

    private String convertToString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Character) {
            return String.valueOf((Character) value);
        }
        return value.toString();
    }

    private List<String> convertToStringList(Object value) {
        if (value == null) {
            return null;
        }
        String str = convertToString(value);
        if (str == null || str.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return java.util.Arrays.asList(str.split(","));
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

        String portsStr = request.getPort();
        String vesselTypesStr = mapper.listToString(request.getVesselTypes());

        if (tariffHdrRepository.existsOverlappingPeriod(
                groupPoidBD, null, request.getPeriodFrom(), request.getPeriodTo(), portsStr, vesselTypesStr)) {
            throw new ValidationException("A tariff with overlapping period already exists for the selected port and vessel types.");
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

        String portsStr = request.getPort();
        String vesselTypesStr = mapper.listToString(request.getVesselTypes());

        if (tariffHdrRepository.existsOverlappingPeriod(
                groupPoidBD, transactionPoid, request.getPeriodFrom(), request.getPeriodTo(), portsStr, vesselTypesStr)) {
            throw new ValidationException("A tariff with overlapping period already exists for the selected port and vessel types.");
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
        if (request.getPort() == null || request.getPort().trim().isEmpty()) {
            throw new ValidationException("Port cannot be empty");
        }
        if (!shipPortMasterRepository.existsByIdPortPoidAndIdGroupPoid(BigDecimal.valueOf(Long.parseLong(request.getPort())), BigDecimal.valueOf(groupPoid))) {
            throw new ResourceNotFoundException("Port", "Port Poid", request.getPort());
        }
        for (String vesselPoid : request.getVesselTypes()) {
            if (!shipVesselTypeMasterRepository.existsByVesselTypePoidAndGroupPoid(BigDecimal.valueOf(Long.parseLong(vesselPoid)), BigDecimal.valueOf(groupPoid))) {
                throw new ResourceNotFoundException("Vessel", "Vessel Poid", vesselPoid);
            }
        }
    }

    private void validateUpdateRequest(PdaPortTariffMasterRequest request, Long groupPoid) {
        if (request.getPort() == null || request.getPort().trim().isEmpty()) {
            throw new ValidationException("Port cannot be empty");
        }
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