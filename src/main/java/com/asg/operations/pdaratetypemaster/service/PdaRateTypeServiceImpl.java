package com.asg.operations.pdaratetypemaster.service;

import com.asg.operations.common.Util.FormulaValidator;
import com.asg.operations.pdaratetypemaster.dto.*;
import com.asg.operations.pdaratetypemaster.util.PdaRateTypeMapper;
import com.asg.operations.pdaratetypemaster.repository.PdaRateTypeRepository;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.pdaratetypemaster.entity.PdaRateTypeMaster;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
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

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PdaRateTypeResponseDTO> getRateTypeList(
            String code,
            String name,
            String active,
            Long groupPoid,
            Pageable pageable
    ) {
        BigDecimal groupPoidBD = BigDecimal.valueOf(groupPoid);

        String normalizedCode = code != null ? code.toUpperCase() : null;
        String normalizedName = name != null ? name.toUpperCase() : null;
        String activeFilter = active != null ? active : "Y";

        Page<PdaRateTypeMaster> rateTypePage = repository.searchRateTypes(
                normalizedCode,
                normalizedName,
                activeFilter,
                groupPoidBD,
                pageable
        );

        List<PdaRateTypeResponseDTO> content = rateTypePage.getContent().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                rateTypePage.getNumber(),
                rateTypePage.getSize(),
                rateTypePage.getTotalElements(),
                rateTypePage.getTotalPages(),
                rateTypePage.isFirst(),
                rateTypePage.isLast(),
                rateTypePage.getNumberOfElements()
        );
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
        return mapper.toResponse(savedRateType);
    }

    @Override
    public PdaRateTypeResponseDTO updateRateType(Long rateTypePoid, PdaRateTypeRequestDTO request, Long groupPoid, String userId) {
        validateUpdateRequest(request, groupPoid);

        BigDecimal groupPoidBD = BigDecimal.valueOf(groupPoid);

        PdaRateTypeMaster existingRateType = repository.findByRateTypePoidAndGroupPoid(rateTypePoid, groupPoidBD)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PdaRateTypeMaster", "rateTypePoid", rateTypePoid));

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

        return mapper.toResponse(existingRateType);
    }

    @Override
    public void deleteRateType(Long rateTypePoid, Long groupPoid, String userId, boolean hardDelete) {
        BigDecimal groupPoidBD = BigDecimal.valueOf(groupPoid);

        PdaRateTypeMaster rateType = repository.findByRateTypePoidAndGroupPoid(rateTypePoid, groupPoidBD)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PdaRateTypeMaster", "rateTypePoid", rateTypePoid));

        if (hardDelete) {
            try {
                repository.delete(rateType);
            } catch (Exception e) {
                throw new ValidationException("Cannot delete rate type due to existing references");
            }
        } else {
            rateType.setActive("N");
            rateType.setDeleted("Y");
            rateType.setLastmodifiedBy(userId);
            rateType.setLastmodifiedDate(LocalDateTime.now());
            repository.save(rateType);
        }
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
