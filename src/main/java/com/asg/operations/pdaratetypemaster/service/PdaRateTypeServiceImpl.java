package com.asg.operations.pdaratetypemaster.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.enums.LogDetailsEnum;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import com.asg.operations.common.Util.FormulaValidator;
import com.asg.operations.commonlov.service.LovService;
import com.asg.operations.pdaratetypemaster.dto.*;
import com.asg.operations.pdaratetypemaster.util.PdaRateTypeMapper;
import com.asg.operations.pdaratetypemaster.repository.PdaRateTypeRepository;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.pdaratetypemaster.entity.PdaRateTypeMaster;
import jakarta.persistence.EntityManager;
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
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PdaRateTypeServiceImpl implements PdaRateTypeService {

    private final PdaRateTypeRepository repository;
    private final PdaRateTypeMapper mapper;
    private final FormulaValidator formulaValidator;
    private final EntityManager entityManager;
    private final LovService lovService;
    private final LoggingService loggingService;
    private final DocumentDeleteService documentDeleteService;

    @Override
    @Transactional(readOnly = true)
    public Page<PdaRateTypeListResponse> getAllRateTypesWithFilters(
            Long groupPoid,
            GetAllRateTypeFilterRequest filterRequest,
            int page, int size, String sort) {

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT r.RATE_TYPE_POID, r.RATE_TYPE_CODE, r.RATE_TYPE_NAME, ");
        sqlBuilder.append("r.RATE_TYPE_FORMULA, r.DEF_DAYS, r.ACTIVE, r.DELETED, ");
        sqlBuilder.append("r.SEQNO, r.CREATED_BY, r.CREATED_DATE, r.LASTMODIFIED_BY, r.LASTMODIFIED_DATE ");
        sqlBuilder.append("FROM PDA_RATE_TYPE_MASTER r ");
        sqlBuilder.append("WHERE r.GROUP_POID = :groupPoid ");

        if (filterRequest.getIsDeleted() != null && "N".equalsIgnoreCase(filterRequest.getIsDeleted())) {
            sqlBuilder.append("AND (r.DELETED IS NULL OR r.DELETED != 'Y') ");
        } else if (filterRequest.getIsDeleted() != null && "Y".equalsIgnoreCase(filterRequest.getIsDeleted())) {
            sqlBuilder.append("AND r.DELETED = 'Y' ");
        }

        List<String> filterConditions = new java.util.ArrayList<>();
        List<GetAllRateTypeFilterRequest.FilterItem> validFilters = new java.util.ArrayList<>();
        if (filterRequest.getFilters() != null && !filterRequest.getFilters().isEmpty()) {
            for (GetAllRateTypeFilterRequest.FilterItem filter : filterRequest.getFilters()) {
                if (StringUtils.hasText(filter.getSearchField()) && StringUtils.hasText(filter.getSearchValue())) {
                    validFilters.add(filter);
                    String columnName = mapRateTypeSearchFieldToColumn(filter.getSearchField());
                    int paramIndex = validFilters.size() - 1;
                    filterConditions.add("LOWER(" + columnName + ") LIKE LOWER(:filterValue" + paramIndex + ")");
                }
            }
        }

        if (!filterConditions.isEmpty()) {
            String operator = "AND".equalsIgnoreCase(filterRequest.getOperator()) ? " AND " : " OR ";
            sqlBuilder.append("AND (").append(String.join(operator, filterConditions)).append(") ");
        }

        String orderBy = "ORDER BY r.RATE_TYPE_CODE ASC";
        if (StringUtils.hasText(sort)) {
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                String sortField = mapRateTypeSortFieldToColumn(sortParts[0].trim());
                String sortDirection = sortParts[1].trim().toUpperCase();
                if ("ASC".equals(sortDirection) || "DESC".equals(sortDirection)) {
                    orderBy = "ORDER BY " + sortField + " " + sortDirection + " NULLS LAST";
                }
            }
        }
        sqlBuilder.append(orderBy);

        String countSql = "SELECT COUNT(*) FROM (" + sqlBuilder.toString() + ")";
        jakarta.persistence.Query query = entityManager.createNativeQuery(sqlBuilder.toString());
        jakarta.persistence.Query countQuery = entityManager.createNativeQuery(countSql);

        query.setParameter("groupPoid", groupPoid);
        countQuery.setParameter("groupPoid", groupPoid);

        if (!validFilters.isEmpty()) {
            for (int i = 0; i < validFilters.size(); i++) {
                GetAllRateTypeFilterRequest.FilterItem filter = validFilters.get(i);
                String paramValue = "%" + filter.getSearchValue() + "%";
                query.setParameter("filterValue" + i, paramValue);
                countQuery.setParameter("filterValue" + i, paramValue);
            }
        }

        Long totalCount = ((Number) countQuery.getSingleResult()).longValue();
        int offset = page * size;
        query.setFirstResult(offset);
        query.setMaxResults(size);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        List<PdaRateTypeListResponse> dtos = results.stream()
                .map(this::mapToRateTypeListResponseDto)
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(dtos, pageable, totalCount);
    }

    private String mapRateTypeSearchFieldToColumn(String searchField) {
        if (searchField == null) return null;
        String normalizedField = searchField.toUpperCase().replace("_", "");
        switch (normalizedField) {
            case "RATETYPECODE":
                return "r.RATE_TYPE_CODE";
            case "RATETYPENAME":
                return "r.RATE_TYPE_NAME";
            case "RATETYPEFORMULA":
                return "r.RATE_TYPE_FORMULA";
            case "ACTIVE":
                return "r.ACTIVE";
            case "DELETED":
                return "r.DELETED";
            default:
                return "r." + searchField.toUpperCase().replace(" ", "_");
        }
    }

    private String mapRateTypeSortFieldToColumn(String sortField) {
        if (sortField == null) return "r.RATE_TYPE_CODE";
        String normalizedField = sortField.toUpperCase().replace("_", "");
        switch (normalizedField) {
            case "RATETYPEPOID":
                return "r.RATE_TYPE_POID";
            case "RATETYPECODE":
                return "r.RATE_TYPE_CODE";
            case "RATETYPENAME":
                return "r.RATE_TYPE_NAME";
            case "RATETYPEFORMULA":
                return "r.RATE_TYPE_FORMULA";
            case "DEFDAYS":
                return "r.DEF_DAYS";
            case "ACTIVE":
                return "r.ACTIVE";
            case "DELETED":
                return "r.DELETED";
            case "SEQNO":
                return "r.SEQNO";
            case "CREATEDBY":
                return "r.CREATED_BY";
            case "CREATEDDATE":
                return "r.CREATED_DATE";
            case "LASTMODIFIEDBY":
                return "r.LASTMODIFIED_BY";
            case "LASTMODIFIEDDATE":
                return "r.LASTMODIFIED_DATE";
            default:
                return "r." + sortField.toUpperCase().replace(" ", "_");
        }
    }

    private PdaRateTypeListResponse mapToRateTypeListResponseDto(Object[] row) {
        PdaRateTypeListResponse dto = new PdaRateTypeListResponse();
        dto.setRateTypeId(row[0] != null ? ((Number) row[0]).longValue() : null);
        dto.setRateTypeCode(convertToString(row[1]));
        dto.setRateTypeName(convertToString(row[2]));
        dto.setRateTypeFormula(convertToString(row[3]));
        dto.setDefDays(row[4] != null ? (BigDecimal) row[4] : null);
        dto.setActive(convertToString(row[5]));
        dto.setSeqNo(row[7] != null ? new BigInteger(row[7].toString()) : null);
        dto.setCreatedBy(convertToString(row[8]));
        dto.setCreatedDate(row[9] != null ? ((java.sql.Timestamp) row[9]).toLocalDateTime() : null);
        dto.setModifiedBy(convertToString(row[10]));
        dto.setModifiedDate(row[11] != null ? ((java.sql.Timestamp) row[11]).toLocalDateTime() : null);
        return dto;
    }

    private String convertToString(Object value) {
        return value != null ? value.toString() : null;
    }

    @Override
    @Transactional(readOnly = true)
    public PdaRateTypeResponseDTO getRateTypeById(Long rateTypePoid, Long groupPoid) {
        BigDecimal groupPoidBD = BigDecimal.valueOf(groupPoid);

        PdaRateTypeMaster rateType = repository.findByRateTypePoidAndGroupPoid(rateTypePoid, groupPoidBD)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PdaRateTypeMaster", "rateTypePoid", rateTypePoid));

        return mapper.toResponse(rateType);
    }

    @Override
    public PdaRateTypeResponseDTO createRateType(PdaRateTypeRequestDTO request, Long groupPoid, String userId) {
        validateCreateRequest(request, groupPoid);

        BigDecimal groupPoidBD = BigDecimal.valueOf(groupPoid);

        String normalizedCode = request.getRateTypeCode() != null
                ? request.getRateTypeCode().trim().toUpperCase()
                : null;

        if (repository.existsByRateTypeCodeAndGroupPoid(normalizedCode, groupPoidBD)) {
            throw new ValidationException("Rate type code already exists: " + normalizedCode);
        }

        String normalizedName = request.getRateTypeName() != null
                ? request.getRateTypeName().trim()
                : null;

        if (normalizedName != null && repository.existsByRateTypeNameAndGroupPoid(normalizedName, groupPoidBD)) {
            throw new ValidationException("Rate type name already exists: " + normalizedName);
        }

        validateFormulaString(request.getRateTypeFormula());

        PdaRateTypeMaster rateType = mapper.toEntity(request, groupPoidBD, userId);

        if (rateType.getSeqno() == null) {
            BigInteger maxSeqno = repository.findMaxSeqnoByGroupPoid(groupPoidBD)
                    .orElse(BigInteger.ZERO);
            rateType.setSeqno(maxSeqno.add(BigInteger.valueOf(10)));
        }

        PdaRateTypeMaster savedRateType = repository.save(rateType);
        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, UserContext.getDocumentId(), savedRateType.getRateTypePoid().toString());
        return mapper.toResponse(savedRateType);
    }

    @Override
    public PdaRateTypeResponseDTO updateRateType(Long rateTypePoid, PdaRateTypeRequestDTO request, Long groupPoid, String userId) {
        validateUpdateRequest(request, groupPoid);

        BigDecimal groupPoidBD = BigDecimal.valueOf(groupPoid);

        PdaRateTypeMaster existingRateType = repository.findByRateTypePoidAndGroupPoid(rateTypePoid, groupPoidBD)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PdaRateTypeMaster", "rateTypePoid", rateTypePoid));

        PdaRateTypeMaster oldRateType = new PdaRateTypeMaster();
        BeanUtils.copyProperties(existingRateType, oldRateType);

        String normalizedName = request.getRateTypeName() != null
                ? request.getRateTypeName().trim()
                : null;

        if (normalizedName != null && !normalizedName.equals(existingRateType.getRateTypeName())) {
            if (repository.existsByRateTypeNameAndGroupPoid(normalizedName, groupPoidBD)) {
                throw new ValidationException("Rate type name already exists: " + normalizedName);
            }
        }

        validateFormulaString(request.getRateTypeFormula());

        mapper.updateEntityFromRequest(existingRateType, request, userId);
        repository.save(existingRateType);

        loggingService.logChanges(oldRateType, existingRateType, PdaRateTypeMaster.class, UserContext.getDocumentId(), existingRateType.getRateTypePoid().toString(), LogDetailsEnum.MODIFIED, "RATE_TYPE_POID");
        return mapper.toResponse(existingRateType);
    }

    @Override
    public void deleteRateType(Long rateTypePoid, Long groupPoid, String userId, boolean hardDelete, @Valid DeleteReasonDto deleteReasonDto) {
        BigDecimal groupPoidBD = BigDecimal.valueOf(groupPoid);

        PdaRateTypeMaster rateType = repository.findByRateTypePoidAndGroupPoid(rateTypePoid, groupPoidBD)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PdaRateTypeMaster", "rateTypePoid", rateTypePoid));

        documentDeleteService.deleteDocument(
                rateTypePoid,
                "PDA_RATE_TYPE_MASTER",
                "RATE_TYPE_POID",
                deleteReasonDto,
                LocalDate.now()
        );

    }

    @Override
    @Transactional(readOnly = true)
    public FormulaValidationResponse validateFormula(FormulaValidationRequest request) {
        List<String> allowableTokens = null;

        if (request.getContext() != null &&
                request.getContext().getAllowableTokens() != null) {
            allowableTokens = request.getContext().getAllowableTokens();
        }

        FormulaValidator.FormulaValidationResult result = formulaValidator.validate(
                request.getFormula(),
                allowableTokens
        );

        FormulaValidationResponse response = new FormulaValidationResponse();
        response.setValid(result.isValid());
        response.setErrors(result.getErrors());
        response.setWarnings(result.getWarnings());
        response.setNormalizedFormula(result.getNormalizedFormula());
        response.setTokens(result.getTokens());

        return response;
    }

    private void validateCreateRequest(PdaRateTypeRequestDTO request, Long groupPoid) {
        if (request.getDefDays() != null &&
                request.getDefDays().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Default days must be ≥ 0");
        }
    }

    private void validateUpdateRequest(PdaRateTypeRequestDTO request, Long groupPoid) {
        if (request.getDefDays() != null &&
                request.getDefDays().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Default days must be ≥ 0");
        }
    }

    private void validateFormulaString(String formula) {
        if (formula != null && !formula.trim().isEmpty()) {
            FormulaValidator.FormulaValidationResult result =
                    formulaValidator.validate(formula, null);

            if (!result.isValid()) {
                throw new ValidationException("Formula validation failed");
            }
        }
    }
}
