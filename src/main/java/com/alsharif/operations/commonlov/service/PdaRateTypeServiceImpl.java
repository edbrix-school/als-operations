package com.alsharif.operations.commonlov.service;

import com.alsharif.operations.common.Util.FormulaValidator;
import com.alsharif.operations.common.Util.PdaRateTypeMapper;
import com.alsharif.operations.commonlov.dto.*;
import com.alsharif.operations.commonlov.repository.PdaRateTypeRepository;
import com.alsharif.operations.exceptions.ResourceNotFoundException;
import com.alsharif.operations.group.entity.PdaRateTypeMaster;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PdaRateTypeServiceImpl implements PdaRateTypeService {

    private final PdaRateTypeRepository repository;
    private final PdaRateTypeMapper mapper;
    private final FormulaValidator formulaValidator;

    // HARD-CODED VALUES (since SecurityContextUtil is removed)
    private static final BigDecimal HARD_CODED_GROUP_POID = BigDecimal.valueOf(1);
    private static final BigDecimal HARD_CODED_USER_POID = BigDecimal.valueOf(1);

    // ----------------------------------------------------------
    // GET LIST
    // ----------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public Page<PdaRateTypeResponseDTO> getRateTypeList(
            String code,
            String name,
            String active,
            Pageable pageable
    ) {
        BigDecimal groupPoid = HARD_CODED_GROUP_POID;

        String normalizedCode = code != null ? code.toUpperCase() : null;
        String normalizedName = name != null ? name.toUpperCase() : null;
        String activeFilter = active != null ? active : "Y";

        Page<PdaRateTypeMaster> rateTypePage = repository.searchRateTypes(
                normalizedCode,
                normalizedName,
                activeFilter,
                groupPoid,
                pageable
        );

        List<PdaRateTypeResponseDTO> responses = mapper.toResponseList(rateTypePage.getContent());

        return new PageImpl<>(responses, pageable, rateTypePage.getTotalElements());
    }

    // ----------------------------------------------------------
    // GET BY ID
    // ----------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public PdaRateTypeResponseDTO getRateTypeById(Long rateTypeId) {
        BigDecimal groupPoid = HARD_CODED_GROUP_POID;

        PdaRateTypeMaster rateType = repository.findByRateTypePoidAndGroupPoid(rateTypeId, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PDA Rate Type",
                        "rateTypeId",
                        rateTypeId
                ));

        return mapper.toResponse(rateType);
    }

    // ----------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------
    @Override
    public PdaRateTypeResponseDTO createRateType(PdaRateTypeRequestDTO request) {
        validateRateTypeRequest(request, null);

        BigDecimal groupPoid = HARD_CODED_GROUP_POID;

        String normalizedCode = request.getRateTypeCode() != null
                ? request.getRateTypeCode().trim().toUpperCase()
                : null;

        if (repository.existsByRateTypeCodeAndGroupPoid(normalizedCode, groupPoid)) {
            throw new ValidationException("Rate type code already exists: " + normalizedCode);
        }

        String normalizedName = request.getRateTypeName() != null
                ? request.getRateTypeName().trim()
                : null;

        if (normalizedName != null && repository.existsByRateTypeNameAndGroupPoid(normalizedName, groupPoid)) {
            throw new ValidationException("Rate type name already exists: " + normalizedName);
        }

        // Validate formula
        validateFormulaString(request.getRateTypeFormula());

        PdaRateTypeMaster rateType = mapper.toEntity(request);

        // Auto-generate seqno
        if (rateType.getSeqno() == null) {
            BigInteger maxSeqno = repository.findMaxSeqnoByGroupPoid(groupPoid)
                    .orElse(BigInteger.ZERO);
            rateType.setSeqno(maxSeqno.add(BigInteger.valueOf(10)));
        }

        rateType = repository.save(rateType);

        return mapper.toResponse(rateType);
    }

    // ----------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------
    @Override
    public PdaRateTypeResponseDTO updateRateType(Long rateTypeId, PdaRateTypeRequestDTO request) {
        validateRateTypeRequest(request, rateTypeId);

        BigDecimal groupPoid = HARD_CODED_GROUP_POID;

        PdaRateTypeMaster rateType = repository.findByRateTypePoidAndGroupPoid(rateTypeId, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PDA Rate Type",
                        "rateTypeId",
                        rateTypeId
                ));

        // Name uniqueness check
        String normalizedName = request.getRateTypeName() != null
                ? request.getRateTypeName().trim()
                : null;

        if (normalizedName != null && !normalizedName.equals(rateType.getRateTypeName())) {
            if (repository.existsByRateTypeNameAndGroupPoid(normalizedName, groupPoid)) {
                throw new ValidationException("Rate type name already exists: " + normalizedName);
            }
        }

        // Validate formula
        validateFormulaString(request.getRateTypeFormula());

        mapper.updateEntity(rateType, request);
        rateType = repository.save(rateType);

        return mapper.toResponse(rateType);
    }

    // ----------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------
    @Override
    public void deleteRateType(Long rateTypeId, boolean hardDelete) {
        BigDecimal groupPoid = HARD_CODED_GROUP_POID;
        BigDecimal userId = HARD_CODED_USER_POID;

        PdaRateTypeMaster rateType = repository.findByRateTypePoidAndGroupPoid(rateTypeId, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PDA Rate Type",
                        "rateTypeId",
                        rateTypeId
                ));

        if (hardDelete) {
            try {
                repository.delete(rateType);
            } catch (Exception e) {
                throw new ValidationException("Cannot delete rate type due to existing references");
            }
        } else {
            rateType.setActive("N");
            rateType.setDeleted("Y");
            rateType.setLastmodifiedBy("system");
            rateType.setLastmodifiedDate(LocalDateTime.now());
            repository.save(rateType);
        }
    }

    // ----------------------------------------------------------
    // FORMULA VALIDATION ENDPOINT SERVICE
    // ----------------------------------------------------------
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

    // ----------------------------------------------------------
    // COMMON VALIDATION
    // ----------------------------------------------------------
    private void validateRateTypeRequest(PdaRateTypeRequestDTO request, Long rateTypeId) {
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
