package com.alsharif.operations.pdaratetypemaster.controller;

import com.alsharif.operations.common.ApiResponse;
import com.alsharif.operations.pdaratetypemaster.dto.*;
import com.alsharif.operations.pdaratetypemaster.service.PdaRateTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pda-rate-types")
@Tag(name = "PDA Rate Type Master", description = "APIs for managing PDA Rate Type Master records")
public class PdaRateTypeController {

    private final PdaRateTypeService rateTypeService;

    @GetMapping
    public ResponseEntity<?> getRateTypeList(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String active,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            Pageable pageable
    ) {
        PageResponse<PdaRateTypeResponseDTO> response = rateTypeService.getRateTypeList(
                code, name, active, groupPoid, pageable);
        return ApiResponse.success("Rate type list retrieved successfully", response);
    }

    @GetMapping("/{rateTypePoid}")
    public ResponseEntity<?> getRateTypeById(
            @PathVariable @NotNull @Positive Long rateTypePoid,
            @RequestHeader("X-Group-Poid") Long groupPoid
    ) {
        PdaRateTypeResponseDTO response = rateTypeService.getRateTypeById(rateTypePoid, groupPoid);
        return ApiResponse.success("Rate type retrieved successfully", response);
    }

    @PostMapping
    public ResponseEntity<?> createRateType(
            @Valid @RequestBody PdaRateTypeRequestDTO request,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        PdaRateTypeResponseDTO response = rateTypeService.createRateType(request, groupPoid, userId);
        return ApiResponse.success("Rate type created successfully", response);
    }

    @PutMapping("/{rateTypePoid}")
    public ResponseEntity<?> updateRateType(
            @PathVariable @NotNull @Positive Long rateTypePoid,
            @Valid @RequestBody PdaRateTypeRequestDTO request,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        PdaRateTypeResponseDTO response = rateTypeService.updateRateType(rateTypePoid, request, groupPoid, userId);
        return ApiResponse.success("Rate type updated successfully", response);
    }



    @DeleteMapping("/{rateTypePoid}")
    public ResponseEntity<?> deleteRateType(
            @PathVariable @NotNull @Positive Long rateTypePoid,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "false") boolean hardDelete
    ) {
        rateTypeService.deleteRateType(rateTypePoid, groupPoid, userId, hardDelete);
        return ApiResponse.success("Rate type deleted successfully");
    }

    @PostMapping("/validate-formula")
    public ResponseEntity<?> validateFormula(
            @Valid @RequestBody FormulaValidationRequest request
    ) {
        FormulaValidationResponse response = rateTypeService.validateFormula(request);
        return ApiResponse.success("Formula validated successfully", response);
    }
}
