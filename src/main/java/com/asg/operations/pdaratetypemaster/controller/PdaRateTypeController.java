package com.asg.operations.pdaratetypemaster.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.operations.common.ApiResponse;
import com.asg.operations.pdaratetypemaster.dto.*;
import com.asg.operations.pdaratetypemaster.service.PdaRateTypeService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.internalServerError;
import static com.asg.common.lib.dto.response.ApiResponse.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/pda-rate-types")
@Tag(name = "PDA Rate Type Master", description = "APIs for managing PDA Rate Type Master records")
public class PdaRateTypeController {

    private final PdaRateTypeService rateTypeService;
    private final LoggingService loggingService;

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/search")
    public ResponseEntity<?> getRateTypeList(
            @RequestBody(required = false) FilterRequestDto filterRequest,
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) LocalDate periodFrom,
            @RequestParam(required = false) LocalDate periodTo
    ) {

        try {
            Map<String, Object> rateTypePage = rateTypeService.getAllRateTypesWithFilters(UserContext.getDocumentId(), filterRequest, pageable, periodFrom, periodTo);
            return success("Rate type list retrieved successfully", rateTypePage);
        }
        catch (Exception ex){
            return internalServerError("Unable to fetch Rate type list: " + ex.getMessage());
        }

    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{rateTypePoid}")
    public ResponseEntity<?> getRateTypeById(
            @PathVariable @NotNull @Positive Long rateTypePoid
    ) {
        PdaRateTypeResponseDTO response = rateTypeService.getRateTypeById(rateTypePoid, UserContext.getGroupPoid());
        loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), rateTypePoid.toString());
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
            @RequestParam(defaultValue = "false") boolean hardDelete,
            @Valid @RequestBody(required = false) DeleteReasonDto deleteReasonDto
    ) {
        rateTypeService.deleteRateType(rateTypePoid, UserContext.getGroupPoid(), UserContext.getUserId(), hardDelete,deleteReasonDto);
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
