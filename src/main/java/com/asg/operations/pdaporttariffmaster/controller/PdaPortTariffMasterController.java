package com.asg.operations.pdaporttariffmaster.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.operations.common.ApiResponse;
import com.asg.operations.pdaporttariffmaster.dto.*;
import com.asg.operations.pdaporttariffmaster.service.PdaPortTariffHdrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/pda-port-tariffs")
@Tag(name = "PDA Port Tariff Master", description = "APIs for managing PDA Port Tariff Master records")
public class PdaPortTariffMasterController {

    private final PdaPortTariffHdrService tariffService;

    @Operation(summary = "Get all Tariffs", description = "Returns paginated list of Tariffs with optional filters. Supports pagination with page and size parameters.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tariff list fetched successfully", content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/search")
    public ResponseEntity<?> getTariffList(
            @RequestBody(required = false) GetAllTariffFilterRequest filterRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {

        // If filterRequest is null, create a default one
        if (filterRequest == null) {
            filterRequest = new GetAllTariffFilterRequest();
            filterRequest.setIsDeleted("N");
            filterRequest.setOperator("AND");
            filterRequest.setFilters(new java.util.ArrayList<>());
        }

        org.springframework.data.domain.Page<PdaPortTariffMasterResponse> tariffPage = tariffService
                .getAllTariffsWithFilters(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), filterRequest, page, size, sort);

        // Create displayFields
        Map<String, String> displayFields = new HashMap<>();
        displayFields.put("PERIOD_FROM", "date");
        displayFields.put("PERIOD_TO", "date");
        displayFields.put("PORTS", "text");

        // Create paginated response with new structure
        Map<String, Object> response = new HashMap<>();
        response.put("content", tariffPage.getContent());
        response.put("pageNumber", tariffPage.getNumber());
        response.put("displayFields", displayFields);
        response.put("pageSize", tariffPage.getSize());
        response.put("totalElements", tariffPage.getTotalElements());
        response.put("totalPages", tariffPage.getTotalPages());
        response.put("last", tariffPage.isLast());

        return ApiResponse.success("Tariff list fetched successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}")
    public ResponseEntity<?> getTariffById(
            @PathVariable @NotNull @Positive Long transactionPoid
    ) {
        PdaPortTariffMasterResponse response = tariffService.getTariffById(transactionPoid, UserContext.getGroupPoid());
        return ApiResponse.success("Tariff retrieved successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping
    public ResponseEntity<?> createTariff(
            @Valid @RequestBody PdaPortTariffMasterRequest request
    ) {
        PdaPortTariffMasterResponse response = tariffService.createTariff(request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
        return ApiResponse.success("Tariff created successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{transactionPoid}")
    public ResponseEntity<?> updateTariff(
            @PathVariable @NotNull @Positive Long transactionPoid,
            @Valid @RequestBody PdaPortTariffMasterRequest request
    ) {
        PdaPortTariffMasterResponse response = tariffService.updateTariff(transactionPoid, request, UserContext.getGroupPoid(), UserContext.getUserId());
        return ApiResponse.success("Tariff updated successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{transactionPoid}")
    public ResponseEntity<?> deleteTariff(
            @PathVariable @NotNull @Positive Long transactionPoid,
            @RequestParam(defaultValue = "false") boolean hardDelete
    ) {
        tariffService.deleteTariff(transactionPoid, UserContext.getGroupPoid(), UserContext.getUserId(), hardDelete);
        return ApiResponse.success("Tariff deleted successfully");
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping("/{transactionPoid}/copy")
    public ResponseEntity<?> copyTariff(
            @PathVariable @NotNull @Positive Long transactionPoid,
            @Valid @RequestBody CopyTariffRequest request
    ) {
        PdaPortTariffMasterResponse response = tariffService.copyTariff(transactionPoid, request, UserContext.getGroupPoid(), UserContext.getUserId());
        return ApiResponse.success("Tariff copied successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/charges")
    public ResponseEntity<?> getChargeDetails(
            @PathVariable @NotNull @Positive Long transactionPoid,
            @RequestParam(defaultValue = "true") boolean includeSlabs
    ) {
        ChargeDetailsResponse response = tariffService.getChargeDetails(transactionPoid, UserContext.getGroupPoid(), includeSlabs);
        return ApiResponse.success("Charge details retrieved successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/charges/bulk")
    public ResponseEntity<?> bulkSaveChargeDetails(
            @PathVariable @NotNull @Positive Long transactionPoid,
            @Valid @RequestBody ChargeDetailsRequest request,
            @RequestHeader("X-User-Id") String userId
    ) {
        ChargeDetailsResponse response = tariffService.bulkSaveChargeDetails(transactionPoid, request, UserContext.getGroupPoid(), userId);
        return ApiResponse.success("Charge details saved successfully", response);
    }
}