package com.alsharif.operations.pdaentryform.controller;


import com.alsharif.operations.pdaporttariffmaster.dto.PageResponse;
import com.alsharif.operations.pdaentryform.dto.*;
import com.alsharif.operations.pdaentryform.service.PdaEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for PDA Entry Form operations
 */
@RestController
@RequestMapping("/api/v1/pda-entries")
@Tag(name = "PDA Entry", description = "APIs for managing PDA Entry forms and related details")
public class PdaEntryController {

    private final PdaEntryService pdaEntryService;

    @Autowired
    public PdaEntryController(PdaEntryService pdaEntryService) {
        this.pdaEntryService = pdaEntryService;
    }

    // ==================== Header CRUD Operations ====================

    @Operation(
            summary = "Get PDA entry list",
            description = "Retrieves a paginated list of PDA entries with optional filtering and sorting. " +
                    "Supports filtering by document reference, transaction reference, principal, status, ref type, " +
                    "vessel, port, and transaction date range. Results are paginated and can be sorted by any field. " +
                    "Only records accessible to the user's company are returned (multi-tenant filtering).",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved PDA entry list",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PageResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<PageResponse<PdaEntryResponse>> getPdaEntryList(
            @Parameter(description = "Document reference (partial match)") @RequestParam(required = false) String docRef,
            @Parameter(description = "Transaction reference (partial match)") @RequestParam(required = false) String transactionRef,
            @Parameter(description = "Principal POID") @RequestParam(required = false) BigDecimal principalPoid,
            @Parameter(description = "Status (e.g., PROPOSAL, CONFIRMED)") @RequestParam(required = false) String status,
            @Parameter(description = "Reference type (GENERAL, ENQUIRY, TDR)") @RequestParam(required = false) String refType,
            @Parameter(description = "Vessel POID") @RequestParam(required = false) BigDecimal vesselPoid,
            @Parameter(description = "Port POID") @RequestParam(required = false) BigDecimal portPoid,
            @Parameter(description = "Transaction date from") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate transactionDateFrom,
            @Parameter(description = "Transaction date to") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate transactionDateTo,
            @Parameter(description = "Deleted flag (Y/N, default: N)") @RequestParam(required = false, defaultValue = "N") String deleted,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort criteria (format: field,direction)") @RequestParam(defaultValue = "transactionDate,desc") String sort,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid
    ) {
        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        PageResponse<PdaEntryResponse> response = pdaEntryService.getPdaEntryList(
                docRef, transactionRef, principalPoid, status, refType,
                vesselPoid, portPoid, transactionDateFrom, transactionDateTo,
                deleted, pageable, groupPoid, companyPoid
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get PDA entry by ID",
            description = "Retrieves a single PDA entry by transaction POID with all header information. " +
                    "Returns the complete entry including all audit fields. " +
                    "The entry must belong to the user's company (multi-tenant filtering).",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved PDA entry",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{transactionPoid}")
    public ResponseEntity<PdaEntryResponse> getPdaEntryById(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid
    ) {
        PdaEntryResponse response = pdaEntryService.getPdaEntryById(transactionPoid, groupPoid, companyPoid);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Create PDA entry",
            description = "Creates a new PDA Entry with header information. " +
                    "Handles default values, auto-population of vessel details and currency, validation, and business logic. " +
                    "Transaction POID and DocRef are auto-generated and retrieved after insert. " +
                    "Calls stored procedures for validation and post-save processing.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully created PDA entry",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<PdaEntryResponse> createPdaEntry(
            @Parameter(description = "PDA Entry request", required = true)
            @Valid @RequestBody PdaEntryRequest request,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        PdaEntryResponse response = pdaEntryService.createPdaEntry(request, groupPoid, companyPoid, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Update PDA entry",
            description = "Updates an existing PDA Entry header. " +
                    "Validates edit permissions based on status and ref type. " +
                    "For TDR ref type, calls edit validation stored procedure. " +
                    "Auto-populates vessel details and currency if changed. " +
                    "Calls stored procedures for validation and post-save processing.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated PDA entry",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Edit not allowed for this entry",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{transactionPoid}")
    public ResponseEntity<PdaEntryResponse> updatePdaEntry(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @Parameter(description = "PDA Entry request", required = true)
            @Valid @RequestBody PdaEntryRequest request,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        PdaEntryResponse response = pdaEntryService.updatePdaEntry(transactionPoid, request, groupPoid, companyPoid, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete PDA entry",
            description = "Soft deletes a PDA Entry by setting DELETED = 'Y'. " +
                    "Validates that deletion is allowed based on status and principal approval. " +
                    "Cannot delete if status is CONFIRMED or CLOSED, or if principal approved for GENERAL ref type.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully deleted PDA entry",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Deletion not allowed for this entry",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{transactionPoid}")
    public ResponseEntity<Map<String, String>> deletePdaEntry(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        pdaEntryService.deletePdaEntry(transactionPoid, groupPoid, companyPoid, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "PDA entry deleted successfully");
        return ResponseEntity.ok(response);
    }

    // ==================== Charge Details Operations ====================

    @Operation(
            summary = "Get charge details",
            description = "Retrieves all charge details for a PDA entry, ordered by sequence number and row ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved charge details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryChargeDetailResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{transactionPoid}/charge-details")
    public ResponseEntity<List<PdaEntryChargeDetailResponse>> getChargeDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid
    ) {
        List<PdaEntryChargeDetailResponse> response = pdaEntryService.getChargeDetails(transactionPoid, groupPoid, companyPoid);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Bulk save charge details",
            description = "Bulk save charge details (create, update, delete in single transaction). " +
                    "For new records, detRowId should be null. For updates, provide existing detRowId. " +
                    "Automatically calculates amounts (QTY × DAYS × PDA_RATE + TAX_AMOUNT) and updates header total. " +
                    "Auto-populates tax information when charge changes.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully saved charge details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryChargeDetailResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Entry cannot be edited",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{transactionPoid}/charge-details/bulk-save")
    public ResponseEntity<List<PdaEntryChargeDetailResponse>> bulkSaveChargeDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @Parameter(description = "Bulk save request with charge details and delete IDs", required = true)
            @Valid @RequestBody BulkSaveChargeDetailsRequest request,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        List<PdaEntryChargeDetailResponse> response = pdaEntryService.bulkSaveChargeDetails(transactionPoid, request, groupPoid, companyPoid, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete charge detail",
            description = "Deletes a single charge detail and recalculates the header total amount.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully deleted charge detail",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Entry cannot be edited",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "PDA entry or charge detail not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{transactionPoid}/charge-details/{detRowId}")
    public ResponseEntity<Map<String, String>> deleteChargeDetail(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @Parameter(description = "Detail row ID", required = true)
            @PathVariable Long detRowId,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        pdaEntryService.deleteChargeDetail(transactionPoid, detRowId, groupPoid, companyPoid, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Charge detail deleted successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Clear all charge details",
            description = "Clears all charge details for a PDA entry by calling stored procedure. " +
                    "Validates that clearing is allowed based on status and ref type. " +
                    "For GENERAL ref type, status must be PROPOSAL. " +
                    "Cannot clear if status is CONFIRMED or CLOSED.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully cleared charge details",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Charge details cannot be cleared",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{transactionPoid}/charge-details/clear")
    public ResponseEntity<Map<String, String>> clearChargeDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        pdaEntryService.clearChargeDetails(transactionPoid, groupPoid, companyPoid, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Charge details cleared successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Recalculate charge details",
            description = "Recalculates all charge details based on current header data using stored procedure. " +
                    "Validates that all required header fields are present (vessel type, GRT, NRT, DWT, port, etc.). " +
                    "Recalculates header total amount after stored procedure execution.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully recalculated charge details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryChargeDetailResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation error - Required header fields missing",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Entry cannot be edited",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{transactionPoid}/recalculate")
    public ResponseEntity<List<PdaEntryChargeDetailResponse>> recalculateChargeDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        List<PdaEntryChargeDetailResponse> response = pdaEntryService.recalculateChargeDetails(transactionPoid, groupPoid, companyPoid, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Load default charges",
            description = "Loads default charges based on header data using stored procedure. " +
                    "Validates that all required header fields are present. " +
                    "Recalculates header total amount after loading default charges.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully loaded default charges",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryChargeDetailResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation error - Required header fields missing",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Entry cannot be edited",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{transactionPoid}/load-default-charges")
    public ResponseEntity<List<PdaEntryChargeDetailResponse>> loadDefaultCharges(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        List<PdaEntryChargeDetailResponse> response = pdaEntryService.loadDefaultCharges(transactionPoid, groupPoid, companyPoid, userId);
        return ResponseEntity.ok(response);
    }

    // ==================== Vehicle Details Operations ====================

    @Operation(
            summary = "Get vehicle details",
            description = "Retrieves all vehicle details for a PDA entry, ordered by row ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved vehicle details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryVehicleDetailResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{transactionPoid}/vehicle-details")
    public ResponseEntity<List<PdaEntryVehicleDetailResponse>> getVehicleDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid
    ) {
        List<PdaEntryVehicleDetailResponse> response = pdaEntryService.getVehicleDetails(transactionPoid, groupPoid, companyPoid);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Bulk save vehicle details",
            description = "Bulk save vehicle details (create, update, delete in single transaction). " +
                    "For new records, detRowId should be null. For updates, provide existing detRowId.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully saved vehicle details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryVehicleDetailResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Entry cannot be edited",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{transactionPoid}/vehicle-details/bulk-save")
    public ResponseEntity<List<PdaEntryVehicleDetailResponse>> bulkSaveVehicleDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @Parameter(description = "Bulk save request with vehicle details and delete IDs", required = true)
            @Valid @RequestBody BulkSaveVehicleDetailsRequest request,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        List<PdaEntryVehicleDetailResponse> response = pdaEntryService.bulkSaveVehicleDetails(transactionPoid, request, groupPoid, companyPoid, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Import vehicle details",
            description = "Imports vehicle details from external source using stored procedure. " +
                    "Validates that entry is editable before importing.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully imported vehicle details",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Entry cannot be edited",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{transactionPoid}/vehicle-details/import")
    public ResponseEntity<Map<String, String>> importVehicleDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        pdaEntryService.importVehicleDetails(transactionPoid, groupPoid, companyPoid, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Vehicle details imported successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Clear vehicle details",
            description = "Clears all vehicle details for a PDA entry using stored procedure. " +
                    "Validates that entry is editable before clearing.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully cleared vehicle details",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Entry cannot be edited",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{transactionPoid}/vehicle-details/clear")
    public ResponseEntity<Map<String, String>> clearVehicleDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        pdaEntryService.clearVehicleDetails(transactionPoid, groupPoid, companyPoid, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Vehicle details cleared successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Publish vehicle details for import",
            description = "Publishes vehicle details for import using stored procedure. " +
                    "Validates that entry is editable before publishing.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully published vehicle details for import",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Entry cannot be edited",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{transactionPoid}/vehicle-details/publish")
    public ResponseEntity<Map<String, String>> publishVehicleDetailsForImport(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        pdaEntryService.publishVehicleDetailsForImport(transactionPoid, groupPoid, companyPoid, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Vehicle details published for import successfully");
        return ResponseEntity.ok(response);
    }

    // ==================== TDR Details Operations ====================

    @Operation(
            summary = "Get TDR details",
            description = "Retrieves all TDR details for a PDA entry, ordered by row ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved TDR details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryTdrDetailResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{transactionPoid}/tdr-details")
    public ResponseEntity<List<PdaEntryTdrDetailResponse>> getTdrDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid
    ) {
        List<PdaEntryTdrDetailResponse> response = pdaEntryService.getTdrDetails(transactionPoid, groupPoid, companyPoid);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Bulk save TDR details",
            description = "Bulk save TDR details (create, update, delete in single transaction). " +
                    "For new records, detRowId should be null. For updates, provide existing detRowId. " +
                    "Note: TDR details are typically read-only in legacy. API supports create/update for future enhancements.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully saved TDR details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryTdrDetailResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Entry cannot be edited",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{transactionPoid}/tdr-details/bulk-save")
    public ResponseEntity<List<PdaEntryTdrDetailResponse>> bulkSaveTdrDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @Parameter(description = "Bulk save request with TDR details and delete IDs", required = true)
            @Valid @RequestBody BulkSaveTdrDetailsRequest request,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        List<PdaEntryTdrDetailResponse> response = pdaEntryService.bulkSaveTdrDetails(transactionPoid, request, groupPoid, companyPoid, userId);
        return ResponseEntity.ok(response);
    }

    // ==================== Acknowledgment Details Operations ====================

    @Operation(
            summary = "Get acknowledgment details",
            description = "Retrieves all acknowledgment details for a PDA entry, ordered by row ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved acknowledgment details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryAcknowledgmentDetailResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{transactionPoid}/acknowledgment-details")
    public ResponseEntity<List<PdaEntryAcknowledgmentDetailResponse>> getAcknowledgmentDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid
    ) {
        List<PdaEntryAcknowledgmentDetailResponse> response = pdaEntryService.getAcknowledgmentDetails(transactionPoid, groupPoid, companyPoid);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Bulk save acknowledgment details",
            description = "Bulk save acknowledgment details (create, update, delete in single transaction). " +
                    "For new records, detRowId should be null. For updates, provide existing detRowId. " +
                    "Note: Acknowledgment details are typically read-only in legacy. API supports create/update for future enhancements.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully saved acknowledgment details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryAcknowledgmentDetailResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Entry cannot be edited",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{transactionPoid}/acknowledgment-details/bulk-save")
    public ResponseEntity<List<PdaEntryAcknowledgmentDetailResponse>> bulkSaveAcknowledgmentDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @Parameter(description = "Bulk save request with acknowledgment details and delete IDs", required = true)
            @Valid @RequestBody BulkSaveAcknowledgmentDetailsRequest request,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        List<PdaEntryAcknowledgmentDetailResponse> response = pdaEntryService.bulkSaveAcknowledgmentDetails(transactionPoid, request, groupPoid, companyPoid, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{transactionPoid}/acknow/upload-details")
    public ResponseEntity<Map<String, String>> uploadAcknowledgmentDetails(
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        pdaEntryService.uploadAcknowledgmentDetails(transactionPoid, groupPoid, companyPoid, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Acknowledgment details uploaded successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{transactionPoid}/acknow/clear-details")
    public ResponseEntity<Map<String, String>> clearAcknowledgmentDetails(
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        pdaEntryService.clearAcknowledgmentDetails(transactionPoid, groupPoid, companyPoid, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Acknowledgment details cleared successfully");
        return ResponseEntity.ok(response);
    }


    // ==================== Special Operations ====================

    @Operation(
            summary = "Validate before save",
            description = "Validates PDA entry before save (called by frontend for real-time validation). " +
                    "Performs all field validations and calls stored procedure for additional validations. " +
                    "Returns validation results with errors and warnings. " +
                    "Transaction POID can be null for new records.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Validation completed",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ValidationResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/validate-before-save")
    public ResponseEntity<ValidationResponse> validateBeforeSave(
            @Parameter(description = "Transaction POID (optional, for existing records)")
            @RequestParam(required = false) Long transactionPoid,
            @Parameter(description = "PDA Entry request to validate", required = true)
            @Valid @RequestBody PdaEntryRequest request,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        ValidationResponse response = pdaEntryService.validateBeforeSave(transactionPoid, request, groupPoid, companyPoid, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Validate after save",
            description = "Validates PDA entry after save (called after successful save). " +
                    "Calls stored procedure for post-save validation and processing. " +
                    "Returns validation results.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Validation completed",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ValidationResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{transactionPoid}/validate-after-save")
    public ResponseEntity<ValidationResponse> validateAfterSave(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        ValidationResponse response = pdaEntryService.validateAfterSave(transactionPoid, groupPoid, companyPoid, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get vessel details",
            description = "Gets vessel details when vessel LOV is changed (auto-population). " +
                    "Calls stored procedure to retrieve vessel type, IMO number, GRT, NRT, and DWT. " +
                    "Transaction POID is optional (can be null for new records).",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved vessel details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = VesselDetailsResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters - Vessel POID is required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/vessel-details")
    public ResponseEntity<VesselDetailsResponse> getVesselDetails(
            @Parameter(description = "Vessel POID", required = true)
            @RequestParam BigDecimal vesselPoid,
            @Parameter(description = "Transaction POID (optional, for existing records)")
            @RequestParam(required = false) Long transactionPoid,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        VesselDetailsResponse response = pdaEntryService.getVesselDetails(vesselPoid, groupPoid, companyPoid, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Create FDA from PDA",
            description = "Creates an FDA (Freight Disbursement Account) from a PDA entry. " +
                    "Calls PROC_PDA_DTL_UPDATE_FDA stored procedure to create FDA in database. " +
                    "Returns FDA_POID which can be used to redirect to FDA screen. " +
                    "Entry must be in editable state.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully created FDA",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "FDA creation failed",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{transactionPoid}/create-fda")
    public ResponseEntity<Map<String, String>> createFda(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") BigDecimal groupPoid,
            @RequestHeader("X-Company-Poid") BigDecimal companyPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        String result = pdaEntryService.createFda(transactionPoid, groupPoid, companyPoid, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "FDA created successfully");
        response.put("result", result);
        return ResponseEntity.ok(response);
    }

    // ==================== Helper Methods ====================

    /**
     * Parse sort parameter (format: "field,direction")
     */
    private Sort parseSort(String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "transactionDate");
        }

        String[] parts = sort.split(",");
        String field = parts[0].trim();
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return Sort.by(direction, field);
    }
}

