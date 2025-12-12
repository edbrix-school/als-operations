package com.asg.operations.pdaratetypemaster.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.operations.common.ApiResponse;
import com.asg.operations.pdaratetypemaster.dto.*;
import com.asg.operations.pdaratetypemaster.service.PdaRateTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/pda-rate-types")
@Tag(name = "PDA Rate Type Master", description = "APIs for managing PDA Rate Type Master records")
public class PdaRateTypeController {

    private final PdaRateTypeService rateTypeService;

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/search")
    public ResponseEntity<?> getRateTypeList(
            @RequestBody(required = false) GetAllRateTypeFilterRequest filterRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        if (filterRequest == null) {
            filterRequest = new GetAllRateTypeFilterRequest();
            filterRequest.setIsDeleted("N");
            filterRequest.setOperator("AND");
            filterRequest.setFilters(new java.util.ArrayList<>());
        }

        org.springframework.data.domain.Page<PdaRateTypeResponseDTO> rateTypePage = rateTypeService
                .getAllRateTypesWithFilters(UserContext.getGroupPoid(), filterRequest, page, size, sort);

        java.util.Map<String, String> displayFields = new java.util.HashMap<>();
        displayFields.put("RATE_TYPE_CODE", "text");
        displayFields.put("RATE_TYPE_NAME", "text");

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("content", rateTypePage.getContent());
        response.put("pageNumber", rateTypePage.getNumber());
        response.put("displayFields", displayFields);
        response.put("pageSize", rateTypePage.getSize());
        response.put("totalElements", rateTypePage.getTotalElements());
        response.put("totalPages", rateTypePage.getTotalPages());
        response.put("last", rateTypePage.isLast());

        return ApiResponse.success("Rate type list retrieved successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{rateTypePoid}")
    public ResponseEntity<?> getRateTypeById(
            @PathVariable @NotNull @Positive Long rateTypePoid
    ) {
        PdaRateTypeResponseDTO response = rateTypeService.getRateTypeById(rateTypePoid, UserContext.getGroupPoid());
        return ApiResponse.success("Rate type retrieved successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping
    public ResponseEntity<?> createRateType(
            @Valid @RequestBody PdaRateTypeRequestDTO request
    ) {
        PdaRateTypeResponseDTO response = rateTypeService.createRateType(request, UserContext.getGroupPoid(), UserContext.getUserId());
        return ApiResponse.success("Rate type created successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{rateTypePoid}")
    public ResponseEntity<?> updateRateType(
            @PathVariable @NotNull @Positive Long rateTypePoid,
            @Valid @RequestBody PdaRateTypeRequestDTO request
    ) {
        PdaRateTypeResponseDTO response = rateTypeService.updateRateType(rateTypePoid, request, UserContext.getGroupPoid(), UserContext.getUserId());
        return ApiResponse.success("Rate type updated successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{rateTypePoid}")
    public ResponseEntity<?> deleteRateType(
            @PathVariable @NotNull @Positive Long rateTypePoid,
            @RequestParam(defaultValue = "false") boolean hardDelete
    ) {
        rateTypeService.deleteRateType(rateTypePoid, UserContext.getGroupPoid(), UserContext.getUserId(), hardDelete);
        return ApiResponse.success("Rate type deleted successfully");
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/validate-formula")
    public ResponseEntity<?> validateFormula(
            @Valid @RequestBody FormulaValidationRequest request
    ) {
        FormulaValidationResponse response = rateTypeService.validateFormula(request);
        return ApiResponse.success("Formula validated successfully", response);
    }
}
