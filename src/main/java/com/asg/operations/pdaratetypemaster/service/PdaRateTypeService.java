package com.asg.operations.pdaratetypemaster.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.operations.pdaratetypemaster.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;

public interface PdaRateTypeService {

    Map<String, Object> getAllRateTypesWithFilters(String documentId, FilterRequestDto filters, Pageable pageable, LocalDate periodFrom, LocalDate periodTo);

    PdaRateTypeResponseDTO getRateTypeById(Long rateTypePoid, Long groupPoid);

    PdaRateTypeResponseDTO createRateType(PdaRateTypeRequestDTO request, Long groupPoid, String userId);

    PdaRateTypeResponseDTO updateRateType(Long rateTypePoid, PdaRateTypeRequestDTO request, Long groupPoid, String userId);

    void deleteRateType(Long rateTypePoid, Long groupPoid, String userId, boolean hardDelete, @Valid DeleteReasonDto deleteReasonDto);

    FormulaValidationResponse validateFormula(FormulaValidationRequest request);
}
