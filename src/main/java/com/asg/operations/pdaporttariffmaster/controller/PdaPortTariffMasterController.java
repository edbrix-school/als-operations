package com.asg.operations.pdaporttariffmaster.controller;

import com.asg.common.lib.security.util.UserContext;
import com.asg.operations.common.ApiResponse;
import com.asg.operations.pdaporttariffmaster.dto.*;
import com.asg.operations.pdaporttariffmaster.service.PdaPortTariffHdrService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/pda-port-tariffs")
@Tag(name = "PDA Port Tariff Master", description = "APIs for managing PDA Port Tariff Master records")
public class PdaPortTariffMasterController {

    private final PdaPortTariffHdrService tariffService;

    @GetMapping
    public ResponseEntity<?> getTariffList(
            @RequestParam(required = false) String portPoid,
            @RequestParam(required = false) LocalDate periodFrom,
            @RequestParam(required = false) LocalDate periodTo,
            @RequestParam(required = false) String vesselTypePoid,
            Pageable pageable
    ) {
        PageResponse<PdaPortTariffMasterResponse> response = tariffService.getTariffList(
                portPoid, periodFrom, periodTo, vesselTypePoid, UserContext.getCompanyPoid(), pageable);
        return ApiResponse.success("Tariff list retrieved successfully", response);
    }

    @GetMapping("/{transactionPoid}")
    public ResponseEntity<?> getTariffById(
            @PathVariable @NotNull @Positive Long transactionPoid
    ) {
        PdaPortTariffMasterResponse response = tariffService.getTariffById(transactionPoid, UserContext.getGroupPoid());
        return ApiResponse.success("Tariff retrieved successfully", response);
    }

    @PostMapping
    public ResponseEntity<?> createTariff(
            @Valid @RequestBody PdaPortTariffMasterRequest request
    ) {
        PdaPortTariffMasterResponse response = tariffService.createTariff(request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
        return ApiResponse.success("Tariff created successfully", response);
    }

    @PutMapping("/{transactionPoid}")
    public ResponseEntity<?> updateTariff(
            @PathVariable @NotNull @Positive Long transactionPoid,
            @Valid @RequestBody PdaPortTariffMasterRequest request
    ) {
        PdaPortTariffMasterResponse response = tariffService.updateTariff(transactionPoid, request, UserContext.getGroupPoid(), UserContext.getUserId());
        return ApiResponse.success("Tariff updated successfully", response);
    }

    @DeleteMapping("/{transactionPoid}")
    public ResponseEntity<?> deleteTariff(
            @PathVariable @NotNull @Positive Long transactionPoid,
            @RequestParam(defaultValue = "false") boolean hardDelete
    ) {
        tariffService.deleteTariff(transactionPoid, UserContext.getGroupPoid(), UserContext.getUserId(), hardDelete);
        return ApiResponse.success("Tariff deleted successfully");
    }

    @PostMapping("/{transactionPoid}/copy")
    public ResponseEntity<?> copyTariff(
            @PathVariable @NotNull @Positive Long transactionPoid,
            @Valid @RequestBody CopyTariffRequest request
    ) {
        PdaPortTariffMasterResponse response = tariffService.copyTariff(transactionPoid, request, UserContext.getGroupPoid(), UserContext.getUserId());
        return ApiResponse.success("Tariff copied successfully", response);
    }

    @GetMapping("/{transactionPoid}/charges")
    public ResponseEntity<?> getChargeDetails(
            @PathVariable @NotNull @Positive Long transactionPoid,
            @RequestParam(defaultValue = "true") boolean includeSlabs
    ) {
        ChargeDetailsResponse response = tariffService.getChargeDetails(transactionPoid, UserContext.getGroupPoid(), includeSlabs);
        return ApiResponse.success("Charge details retrieved successfully", response);
    }

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