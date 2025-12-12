package com.asg.operations.pdaratetypemaster.service;

import com.asg.operations.pdaratetypemaster.dto.*;

public interface PdaRateTypeService {

    org.springframework.data.domain.Page<PdaRateTypeListResponse> getAllRateTypesWithFilters(Long groupPoid, GetAllRateTypeFilterRequest filterRequest, int page, int size, String sort);

    PdaRateTypeResponseDTO getRateTypeById(Long rateTypePoid, Long groupPoid);

    PdaRateTypeResponseDTO createRateType(PdaRateTypeRequestDTO request, Long groupPoid, String userId);

    PdaRateTypeResponseDTO updateRateType(Long rateTypePoid, PdaRateTypeRequestDTO request, Long groupPoid, String userId);

    void deleteRateType(Long rateTypePoid, Long groupPoid, String userId, boolean hardDelete);

    FormulaValidationResponse validateFormula(FormulaValidationRequest request);
}
