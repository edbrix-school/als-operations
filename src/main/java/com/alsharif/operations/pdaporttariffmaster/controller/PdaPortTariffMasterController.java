package com.alsharif.operations.pdaporttariffmaster.controller;

import com.alsharif.operations.common.ApiResponse;
import com.alsharif.operations.pdaporttariffmaster.dto.*;
import com.alsharif.operations.pdaporttariffmaster.service.PdaPortTariffHdrService;
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
@RequestMapping("/api/v1/pda-port-tariffs")
@Tag(name = "PDA Port Tariff Master", description = "APIs for managing PDA Port Tariff Master records")
public class PdaPortTariffMasterController {

    private final PdaPortTariffHdrService tariffService;

    @GetMapping
    public ResponseEntity<?> getTariffList(
            @RequestParam(required = false) String portPoid,
            @RequestParam(required = false) LocalDate periodFrom,
            @RequestParam(required = false) LocalDate periodTo,
            @RequestParam(required = false) String vesselTypePoid,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            Pageable pageable
    ) {
        PageResponse<PdaPortTariffMasterResponse> response = tariffService.getTariffList(
                portPoid, periodFrom, periodTo, vesselTypePoid, groupPoid, pageable);
        return ApiResponse.success("Tariff list retrieved successfully", response);
    }

    @GetMapping("/{transactionPoid}")
    public ResponseEntity<?> getTariffById(
            @PathVariable @NotNull @Positive Long transactionPoid,
            @RequestHeader("X-Group-Poid") Long groupPoid
    ) {
        PdaPortTariffMasterResponse response = tariffService.getTariffById(transactionPoid, groupPoid);
        return ApiResponse.success("Tariff retrieved successfully", response);
    }

    @PostMapping
    public ResponseEntity<?> createTariff(
            @Valid @RequestBody PdaPortTariffMasterRequest request,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-Company-Poid") Long companyPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        PdaPortTariffMasterResponse response = tariffService.createTariff(request, groupPoid, companyPoid, userId);
        return ApiResponse.success("Tariff created successfully", response);
    }

    @PutMapping("/{transactionPoid}")
    public ResponseEntity<?> updateTariff(
            @PathVariable @NotNull @Positive Long transactionPoid,
            @Valid @RequestBody PdaPortTariffMasterRequest request,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        PdaPortTariffMasterResponse response = tariffService.updateTariff(transactionPoid, request, groupPoid, userId);
        return ApiResponse.success("Tariff updated successfully", response);
    }

    @DeleteMapping("/{transactionPoid}")
    public ResponseEntity<?> deleteTariff(
            @PathVariable @NotNull @Positive Long transactionPoid,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "false") boolean hardDelete
    ) {
        tariffService.deleteTariff(transactionPoid, groupPoid, userId, hardDelete);
        return ApiResponse.success("Tariff deleted successfully");
    }

    @PostMapping("/{transactionPoid}/copy")
    public ResponseEntity<?> copyTariff(
            @PathVariable @NotNull @Positive Long transactionPoid,
            @Valid @RequestBody CopyTariffRequest request,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        PdaPortTariffMasterResponse response = tariffService.copyTariff(transactionPoid, request, groupPoid, userId);
        return ApiResponse.success("Tariff copied successfully", response);
    }

    @GetMapping("/{transactionPoid}/charges")
    public ResponseEntity<?> getChargeDetails(
            @PathVariable @NotNull @Positive Long transactionPoid,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestParam(defaultValue = "true") boolean includeSlabs
    ) {
        ChargeDetailsResponse response = tariffService.getChargeDetails(transactionPoid, groupPoid, includeSlabs);
        return ApiResponse.success("Charge details retrieved successfully", response);
    }

    @PostMapping("/{transactionPoid}/charges/bulk")
    public ResponseEntity<?> bulkSaveChargeDetails(
            @PathVariable @NotNull @Positive Long transactionPoid,
            @Valid @RequestBody ChargeDetailsRequest request,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        ChargeDetailsResponse response = tariffService.bulkSaveChargeDetails(transactionPoid, request, groupPoid, userId);
        return ApiResponse.success("Charge details saved successfully", response);
    }
}