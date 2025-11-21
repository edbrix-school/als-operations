package com.alsharif.operations.commonlov.service;

import com.alsharif.operations.commonlov.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PdaRateTypeService {

    Page<PdaRateTypeResponseDTO> getRateTypeList(
            String code,
            String name,
            String active,
            Pageable pageable
    );

    PdaRateTypeResponseDTO getRateTypeById(Long rateTypeId);

    PdaRateTypeResponseDTO createRateType(PdaRateTypeRequestDTO request);

    PdaRateTypeResponseDTO updateRateType(Long rateTypeId, PdaRateTypeRequestDTO request);

    void deleteRateType(Long rateTypeId, boolean hardDelete);

    FormulaValidationResponse validateFormula(FormulaValidationRequest request);
}
