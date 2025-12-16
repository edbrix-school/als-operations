package com.asg.operations.pdaRoRoVehicle.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.operations.common.ApiResponse;
import com.asg.operations.pdaRoRoVehicle.dto.*;
import com.asg.operations.pdaRoRoVehicle.service.PdaRoroEntryService;
import com.asg.operations.pdaporttariffmaster.dto.GetAllTariffFilterRequest;
import com.asg.operations.pdaporttariffmaster.dto.PdaPortTariffListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/pda-roro-entries")
@Tag(
        name = "PDA Ro-Ro Entry",
        description = "APIs for managing PDA Ro-Ro Vehicle Entry transactions"
)
public class PdaRoroEntryController {

    private final PdaRoroEntryService pdaRoroEntryService;


    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping
    public ResponseEntity<?> createRoroEntry(
            @Valid @RequestBody PdaRoroEntryHdrRequestDto request
    ) {

        PdaRoroEntryHdrResponseDto response =
                pdaRoroEntryService.createRoroEntry(request);

        return ApiResponse.success("PDA Ro-Ro Entry created successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{transactionPoid}")
    public ResponseEntity<?> updateRoroEntry(
            @PathVariable @NotNull @Positive
            Long transactionPoid,

            @Valid @RequestBody
            PdaRoroEntryHdrRequestDto request
    ) {

        PdaRoroEntryHdrResponseDto response =
                pdaRoroEntryService.updateRoroEntry(transactionPoid, request);

        return ApiResponse.success("PDA Ro-Ro Entry updated successfully", response);
    }


    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}")
    public ResponseEntity<?> getRoroEntryById(
            @PathVariable @NotNull @Positive
            Long transactionPoid
    ) {

        PdaRoroEntryHdrResponseDto response =
                pdaRoroEntryService.getRoroEntry(transactionPoid);

        return ApiResponse.success("PDA Ro-Ro Entry retrieved successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{transactionPoid}")
    public ResponseEntity<?> deleteRoRoEntry(
            @PathVariable @NotNull @Positive Long transactionPoid

    ) {
        pdaRoroEntryService.deleteRoRoEntry(transactionPoid);
        return ApiResponse.success("Tariff deleted successfully");
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping("/{transactionPoid}/upload-vehicle-details")
    public ResponseEntity<?> uploadVehicleDetails(
            @PathVariable @NotNull @Positive
            Long transactionPoid,

            @Valid @RequestBody
            PdaRoroVehicleUploadRequest request
    ) {

        request.setTransactionPoid(transactionPoid);

        PdaRoroVehicleUploadResponse response =
                pdaRoroEntryService.uploadVehicleDetails(request);

        return ApiResponse.success("Vehicle details uploaded successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/clear-vehicle-details")
    public ResponseEntity<?> clearVehicleDetails(
            @PathVariable @NotNull @Positive Long transactionPoid
    ) {

        String status = pdaRoroEntryService.clearRoroVehicleDetails(transactionPoid);

        return ApiResponse.success(status);
    }

    @Operation(summary = "Get all Ro Ro Vehicle", description = "Returns paginated list of Ro Ro Vehicle with optional filters. Supports pagination with page and size parameters.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ro Ro Vehicle list fetched successfully", content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/search")
    public ResponseEntity<?> getRoRoVehicleList(
            @RequestBody(required = false) GetAllRoRoVehicleFilterRequest filterRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {


        if (filterRequest == null) {
            filterRequest = new GetAllRoRoVehicleFilterRequest();
            filterRequest.setIsDeleted("N");
            filterRequest.setOperator("AND");
            filterRequest.setFilters(new java.util.ArrayList<>());
        }

        org.springframework.data.domain.Page<RoRoVehicleListResponse> roroVehiclePage = pdaRoroEntryService
                .getRoRoVehicleList(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), filterRequest, page, size, sort);

        // Create displayFields
        Map<String, String> displayFields = new LinkedHashMap<>();
        displayFields.put("PERIOD_FROM", "date");
        displayFields.put("PERIOD_TO", "date");
        displayFields.put("PORT_NAME", "text");

        // Create paginated response with new structure
        Map<String, Object> response = new HashMap<>();
        response.put("content", roroVehiclePage.getContent());
        response.put("pageNumber", roroVehiclePage.getNumber());
        response.put("displayFields", displayFields);
        response.put("pageSize", roroVehiclePage.getSize());
        response.put("totalElements", roroVehiclePage.getTotalElements());
        response.put("totalPages", roroVehiclePage.getTotalPages());
        response.put("last", roroVehiclePage.isLast());

        return ApiResponse.success("Ro Ro Vehicle list fetched successfully", response);
    }


}
