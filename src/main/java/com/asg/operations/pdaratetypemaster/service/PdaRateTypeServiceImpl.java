package com.asg.operations.pdaratetypemaster.service;

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
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import com.asg.operations.common.Util.FormulaValidator;
import com.asg.operations.pdaratetypemaster.dto.*;
import com.asg.operations.pdaratetypemaster.util.PdaRateTypeMapper;
import com.asg.operations.pdaratetypemaster.repository.PdaRateTypeRepository;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.pdaratetypemaster.entity.PdaRateTypeMaster;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class PdaRateTypeServiceImpl implements PdaRateTypeService {

    private final PdaRateTypeRepository repository;
    private final PdaRateTypeMapper mapper;
    private final FormulaValidator formulaValidator;
    private final LoggingService loggingService;
    private final DocumentDeleteService documentDeleteService;
    private final DocumentSearchService documentSearchService;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAllRateTypesWithFilters(
            String documentId, FilterRequestDto filterRequestDto, Pageable pageable, LocalDate periodFrom, LocalDate periodTo) {

        String operator = documentSearchService.resolveOperator(filterRequestDto);
        String isDeleted = documentSearchService.resolveIsDeleted(filterRequestDto);
        List<FilterDto> filters = documentSearchService.resolveDateFilters(filterRequestDto, "TRANSACTION_DATE", periodFrom, periodTo);

        RawSearchResult raw = documentSearchService.search(documentId, filters, operator, pageable, isDeleted,
                "RATE_TYPE_CODE",
                "RATE_TYPE_POID");

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
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

        String normalizedCode = StringUtils.isNotBlank(request.getRateTypeCode()) ? request.getRateTypeCode().trim().toUpperCase() : null;

        if (StringUtils.isNotBlank(normalizedCode) && normalizedCode.contains(" ")) {
            throw new ValidationException("Rate type code must not contain spaces");
        }

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
    public void deleteRateType(Long rateTypePoid, Long groupPoid, String userId, @Valid DeleteReasonDto deleteReasonDto) {
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
