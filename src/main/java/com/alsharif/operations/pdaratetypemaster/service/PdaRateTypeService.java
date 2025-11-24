package com.alsharif.operations.pdaratetypemaster.service;

import com.alsharif.operations.pdaratetypemaster.dto.*;
import org.springframework.data.domain.Pageable;

public interface PdaRateTypeService {

    PageResponse<PdaRateTypeResponseDTO> getRateTypeList(
            String code,
            String name,
            String active,
            Long groupPoid,
            Pageable pageable
    );

    PdaRateTypeResponseDTO getRateTypeById(Long rateTypePoid, Long groupPoid);

    PdaRateTypeResponseDTO createRateType(PdaRateTypeRequestDTO request, Long groupPoid, String userId);

    PdaRateTypeResponseDTO updateRateType(Long rateTypePoid, PdaRateTypeRequestDTO request, Long groupPoid, String userId);

    void deleteRateType(Long rateTypePoid, Long groupPoid, String userId, boolean hardDelete);

    FormulaValidationResponse validateFormula(FormulaValidationRequest request);
}
