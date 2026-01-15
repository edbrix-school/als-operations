package com.asg.operations.pdaRoRoVehicle.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.operations.common.ApiResponse;
import com.asg.operations.pdaRoRoVehicle.dto.*;
import com.asg.operations.pdaRoRoVehicle.service.PdaRoRoEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.internalServerError;
import static com.asg.common.lib.dto.response.ApiResponse.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/pda-roro-entries")
@Tag(
        name = "PDA Ro-Ro Entry",
        description = "APIs for managing PDA Ro-Ro Vehicle Entry transactions"
)
public class PdaRoRoEntryController {

    private final PdaRoRoEntryService pdaRoroEntryService;
    private final LoggingService loggingService;

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping
    public ResponseEntity<?> createRoRoEntry(@Valid @RequestBody PdaRoroEntryHdrRequestDto request) {
        PdaRoRoEntryHdrResponseDto response = pdaRoroEntryService.createRoRoEntry(request);
        return ApiResponse.success("PDA Ro-Ro Entry created successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{transactionPoid}")
    public ResponseEntity<?> updateRoRoEntry(
            @PathVariable @NotNull @Positive Long transactionPoid,
            @Valid @RequestBody PdaRoroEntryHdrRequestDto request) {
        PdaRoRoEntryHdrResponseDto response = pdaRoroEntryService.updateRoRoEntry(transactionPoid, request);
        return ApiResponse.success("PDA Ro-Ro Entry updated successfully", response);
    }


    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}")
    public ResponseEntity<?> getRoRoEntryById(@PathVariable @NotNull @Positive Long transactionPoid) {
        PdaRoRoEntryHdrResponseDto response = pdaRoroEntryService.getRoRoEntry(transactionPoid);
        loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), transactionPoid.toString());
        return ApiResponse.success("PDA Ro-Ro Entry retrieved successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{transactionPoid}")
    public ResponseEntity<?> deleteRoRoEntry(@PathVariable @NotNull @Positive Long transactionPoid,@Valid @RequestBody(required = false) DeleteReasonDto deleteReasonDto) {
        pdaRoroEntryService.deleteRoRoEntry(transactionPoid,deleteReasonDto);
        return ApiResponse.success("PDA Ro-Ro Entry deleted successfully");
    }

    @Operation(
            summary = "Get all Ro Ro Vehicle",
            description = "Returns paginated list of Ro Ro Vehicle with optional filters. Supports pagination with page and size parameters.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Ro Ro Vehicle list fetched successfully",
                            content = @Content(schema = @Schema(implementation = Page.class))
                    )
            }
    )
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/search")
    public ResponseEntity<?> getRoRoVehicleList(
            @RequestBody(required = false) FilterRequestDto filterRequest,
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) LocalDate periodFrom,
            @RequestParam(required = false) LocalDate periodTo) {

        try {
            Map<String, Object> rateTypePage = pdaRoroEntryService.getRoRoVehicleList(UserContext.getDocumentId(), filterRequest, pageable, periodFrom, periodTo);
            return success("Ro Ro Vehicle list fetched successfully", rateTypePage);
        }
        catch (Exception ex){
            return internalServerError("Unable to fetch Ro Ro Vehicle list: " + ex.getMessage());
        }

    }

    @Operation(
            summary = "Import Excel file",
            description = "Upload and import Excel file to temp table for RoRo vehicle details"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    mediaType = "multipart/form-data",
                    schema = @Schema(type = "object", implementation = Object.class)
            )
    )
    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping(value = "/upload-excel", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadExcel(
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Excel file to upload",
                    required = true,
                    content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            )
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        String result = pdaRoroEntryService.uploadExcel(file);
        return ApiResponse.success(result);
    }

    @Operation(
            summary = "Upload vehicle details",
            description = "Upload RoRo vehicle details from temp table to main table"
    )
    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping("/upload-vehicle-details")
    public ResponseEntity<?> uploadVehicleDetails(@Valid @RequestBody PdaRoRoVehicleUploadRequest request) {
        PdaRoroVehicleUploadResponse response = pdaRoroEntryService.uploadVehicleDetails(request);
        return ApiResponse.success("RoRo vehicle details uploaded successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/clear-vehicle-details")
    public ResponseEntity<?> clearVehicleDetails(@PathVariable @NotNull @Positive Long transactionPoid) {
        String status = pdaRoroEntryService.clearRoRoVehicleDetails(transactionPoid);
        return ApiResponse.success(status);
    }
}
