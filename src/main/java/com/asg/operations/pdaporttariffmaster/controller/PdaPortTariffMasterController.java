package com.asg.operations.pdaporttariffmaster.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.operations.common.ApiResponse;
import com.asg.operations.pdaporttariffmaster.dto.*;
import com.asg.operations.pdaporttariffmaster.service.PdaPortTariffHdrService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.internalServerError;
import static com.asg.common.lib.dto.response.ApiResponse.success;

@RestController
@RequestMapping("/v1/pda-port-tariffs")
@Tag(name = "PDA Port Tariff Master", description = "APIs for managing PDA Port Tariff Master records")
public class PdaPortTariffMasterController {

    private final PdaPortTariffHdrService tariffService;
    private final LoggingService loggingService;

    public PdaPortTariffMasterController(PdaPortTariffHdrService tariffService, LoggingService loggingService) {
        this.tariffService = tariffService;
        this.loggingService = loggingService;
    }

    @Operation(summary = "Get all Tariffs", description = "Returns paginated list of Tariffs with optional filters. Supports pagination with page and size parameters.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tariff list fetched successfully", content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/search")
    public ResponseEntity<?> getTariffList(
            @RequestBody(required = false) FilterRequestDto filterRequest,
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) LocalDate periodFrom,
            @RequestParam(required = false) LocalDate periodTo) {

        try {
            Map<String, Object> tariffPage = tariffService.getAllTariffsWithFilters(UserContext.getDocumentId(), filterRequest, pageable, periodFrom, periodTo);
            return success("Tariff list fetched successfully", tariffPage);
        }
        catch (Exception ex){
            return internalServerError("Unable to fetch tariff list: " + ex.getMessage());
        }

    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}")
    public ResponseEntity<?> getTariffById(
            @PathVariable @NotNull @Positive Long transactionPoid
    ) {
        PdaPortTariffMasterResponse response = tariffService.getTariffById(transactionPoid, UserContext.getGroupPoid());
        loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), transactionPoid.toString());
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
            @RequestParam(defaultValue = "false") boolean hardDelete,
            @Valid @RequestBody(required = false) DeleteReasonDto deleteReasonDto
    ) {
        tariffService.deleteTariff(transactionPoid, UserContext.getGroupPoid(), UserContext.getUserId(), hardDelete,deleteReasonDto);
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