package com.asg.operations.pdaentryform.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.operations.pdaentryform.dto.*;
import com.asg.operations.pdaporttariffmaster.dto.PageResponse;
import com.asg.operations.pdaentryform.service.PdaEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.asg.operations.common.ApiResponse;

/**
 * REST Controller for PDA Entry Form operations
 */
@RestController
@RequestMapping("/v1/pda-entries")
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
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved PDA entry list",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PageResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/search")
    public ResponseEntity<?> getPdaEntryList(
            @RequestBody(required = false) GetAllPdaFilterRequest filterRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {

        // If filterRequest is null, create a default one
        if (filterRequest == null) {
            filterRequest = new GetAllPdaFilterRequest();
            filterRequest.setIsDeleted("N");
            filterRequest.setOperator("AND");
            filterRequest.setFilters(new java.util.ArrayList<>());
        }

        org.springframework.data.domain.Page<PdaEntryListResponse> pdaPage = pdaEntryService
                .getAllPdaWithFilters(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), filterRequest, page, size, sort);

        // Create displayFields
        Map<String, String> displayFields = new HashMap<>();
        displayFields.put("TRANSACTION_DATE", "date");
        displayFields.put("DOC_REF", "text");
        displayFields.put("FDA_REF", "text");
        displayFields.put("PRINCIPAL_NAME", "text");
        displayFields.put("VESSEL_NAME", "text");
        displayFields.put("VOYAGE_NO", "text");

        // Create paginated response with new structure
        Map<String, Object> response = new HashMap<>();
        response.put("content", pdaPage.getContent());
        response.put("pageNumber", pdaPage.getNumber());
        response.put("displayFields", displayFields);
        response.put("pageSize", pdaPage.getSize());
        response.put("totalElements", pdaPage.getTotalElements());
        response.put("totalPages", pdaPage.getTotalPages());
        response.put("last", pdaPage.isLast());

        return ApiResponse.success("PDA entry list fetched successfully", response);
    }

    @Operation(
            summary = "Get PDA entry by ID",
            description = "Retrieves a single PDA entry by transaction POID with all header information. " +
                    "Returns the complete entry including all audit fields. " +
                    "The entry must belong to the user's company (multi-tenant filtering).",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved PDA entry",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}")
    public ResponseEntity<?> getPdaEntryById(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid
    ) {
        PdaEntryResponse response = pdaEntryService.getPdaEntryById(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
        return ApiResponse.success("PDA entry retrieved successfully", response);
    }

    @Operation(
            summary = "Create PDA entry",
            description = "Creates a new PDA Entry with header information. " +
                    "Handles default values, auto-population of vessel details and currency, validation, and business logic. " +
                    "Transaction POID and DocRef are auto-generated and retrieved after insert. " +
                    "Calls stored procedures for validation and post-save processing.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully created PDA entry",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping
    public ResponseEntity<?> createPdaEntry(
            @Parameter(description = "PDA Entry request", required = true)
            @Valid @RequestBody PdaEntryRequest request
    ) {
        PdaEntryResponse response = pdaEntryService.createPdaEntry(request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return ApiResponse.success("PDA entry created successfully", response);
    }

    @Operation(
            summary = "Update PDA entry",
            description = "Updates an existing PDA Entry header. " +
                    "Validates edit permissions based on status and ref type. " +
                    "For TDR ref type, calls edit validation stored procedure. " +
                    "Auto-populates vessel details and currency if changed. " +
                    "Calls stored procedures for validation and post-save processing.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated PDA entry",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Edit not allowed for this entry",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{transactionPoid}")
    public ResponseEntity<?> updatePdaEntry(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @Parameter(description = "PDA Entry request", required = true)
            @Valid @RequestBody PdaEntryRequest request
    ) {
        PdaEntryResponse response = pdaEntryService.updatePdaEntry(transactionPoid, request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return ApiResponse.success("PDA entry updated successfully", response);
    }

    @Operation(
            summary = "Delete PDA entry",
            description = "Soft deletes a PDA Entry by setting DELETED = 'Y'. " +
                    "Validates that deletion is allowed based on status and principal approval. " +
                    "Cannot delete if status is CONFIRMED or CLOSED, or if principal approved for GENERAL ref type.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully deleted PDA entry",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Deletion not allowed for this entry",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{transactionPoid}")
    public ResponseEntity<?> deletePdaEntry(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid
    ) {
        pdaEntryService.deletePdaEntry(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return ApiResponse.success("PDA entry deleted successfully", null);
    }

    // ==================== Charge Details Operations ====================

    @Operation(
            summary = "Get charge details",
            description = "Retrieves all charge details for a PDA entry, ordered by sequence number and row ID.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved charge details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryChargeDetailResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/charge-details")
    public ResponseEntity<?> getChargeDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid
    ) {
        List<PdaEntryChargeDetailResponse> response = pdaEntryService.getChargeDetails(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
        return ApiResponse.success("Charge details retrieved successfully", response);
    }

    @Operation(
            summary = "Bulk save charge details",
            description = "Bulk save charge details (create, update, delete in single transaction). " +
                    "For new records, detRowId should be null. For updates, provide existing detRowId. " +
                    "Automatically calculates amounts (QTY × DAYS × PDA_RATE + TAX_AMOUNT) and updates header total. " +
                    "Auto-populates tax information when charge changes.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully saved charge details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryChargeDetailResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Entry cannot be edited",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/charge-details/bulk-save")
    public ResponseEntity<?> bulkSaveChargeDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @Parameter(description = "Bulk save request with charge details and delete IDs", required = true)
            @Valid @RequestBody BulkSaveChargeDetailsRequest request
    ) {
        List<PdaEntryChargeDetailResponse> response = pdaEntryService.bulkSaveChargeDetails(transactionPoid, request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
        return ApiResponse.success("Charge details saved successfully", response);
    }

    @Operation(
            summary = "Delete charge detail",
            description = "Deletes a single charge detail and recalculates the header total amount.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully deleted charge detail",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Entry cannot be edited",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "PDA entry or charge detail not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{transactionPoid}/charge-details/{detRowId}")
    public ResponseEntity<?> deleteChargeDetail(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @Parameter(description = "Detail row ID", required = true)
            @PathVariable Long detRowId
    ) {
        pdaEntryService.deleteChargeDetail(transactionPoid, detRowId, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
        return ApiResponse.success("Charge detail deleted successfully", null);
    }

    @Operation(
            summary = "Clear all charge details",
            description = "Clears all charge details for a PDA entry by calling stored procedure. " +
                    "Validates that clearing is allowed based on status and ref type. " +
                    "For GENERAL ref type, status must be PROPOSAL. " +
                    "Cannot clear if status is CONFIRMED or CLOSED.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully cleared charge details",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Charge details cannot be cleared",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/charge-details/clear")
    public ResponseEntity<?> clearChargeDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid
    ) {
        pdaEntryService.clearChargeDetails(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return ApiResponse.success("Charge details cleared successfully", null);
    }

    @Operation(
            summary = "Recalculate charge details",
            description = "Recalculates all charge details based on current header data using stored procedure. " +
                    "Validates that all required header fields are present (vessel type, GRT, NRT, DWT, port, etc.). " +
                    "Recalculates header total amount after stored procedure execution.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully recalculated charge details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryChargeDetailResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Validation error - Required header fields missing",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Entry cannot be edited",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/recalculate")
    public ResponseEntity<?> recalculateChargeDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid
    ) {
        List<PdaEntryChargeDetailResponse> response = pdaEntryService.recalculateChargeDetails(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return ApiResponse.success("Charge details recalculated successfully", response);
    }

    @Operation(
            summary = "Load default charges",
            description = "Loads default charges based on header data using stored procedure. " +
                    "Validates that all required header fields are present. " +
                    "Recalculates header total amount after loading default charges.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully loaded default charges",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryChargeDetailResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Validation error - Required header fields missing",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Entry cannot be edited",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/{transactionPoid}/load-default-charges")
    public ResponseEntity<?> loadDefaultCharges(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid
    ) {
        List<PdaEntryChargeDetailResponse> response = pdaEntryService.loadDefaultCharges(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return ApiResponse.success("Default charges loaded successfully", response);
    }

    // ==================== Vehicle Details Operations ====================

    @Operation(
            summary = "Get vehicle details",
            description = "Retrieves all vehicle details for a PDA entry, ordered by row ID.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved vehicle details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryVehicleDetailResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/vehicle-details")
    public ResponseEntity<?> getVehicleDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid
    ) {
        List<PdaEntryVehicleDetailResponse> response = pdaEntryService.getVehicleDetails(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
        return ApiResponse.success("Vehicle details retrieved successfully", response);
    }

    @Operation(
            summary = "Bulk save vehicle details",
            description = "Bulk save vehicle details (create, update, delete in single transaction). " +
                    "For new records, detRowId should be null. For updates, provide existing detRowId.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully saved vehicle details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryVehicleDetailResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Entry cannot be edited",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/vehicle-details/bulk-save")
    public ResponseEntity<?> bulkSaveVehicleDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @Parameter(description = "Bulk save request with vehicle details and delete IDs", required = true)
            @Valid @RequestBody BulkSaveVehicleDetailsRequest request
    ) {
        List<PdaEntryVehicleDetailResponse> response = pdaEntryService.bulkSaveVehicleDetails(transactionPoid, request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
        return ApiResponse.success("Vehicle details saved successfully", response);
    }

    @Operation(
            summary = "Import vehicle details",
            description = "Imports vehicle details from external source using stored procedure. " +
                    "Validates that entry is editable before importing.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully imported vehicle details",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Entry cannot be edited",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/vehicle-details/import")
    public ResponseEntity<?> importVehicleDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid
    ) {
        pdaEntryService.importVehicleDetails(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return ApiResponse.success("Vehicle details imported successfully", null);
    }

    @Operation(
            summary = "Clear vehicle details",
            description = "Clears all vehicle details for a PDA entry using stored procedure. " +
                    "Validates that entry is editable before clearing.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully cleared vehicle details",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Entry cannot be edited",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/vehicle-details/clear")
    public ResponseEntity<?> clearVehicleDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid
    ) {
        pdaEntryService.clearVehicleDetails(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return ApiResponse.success("Vehicle details cleared successfully", null);
    }

    @Operation(
            summary = "Publish vehicle details for import",
            description = "Publishes vehicle details for import using stored procedure. " +
                    "Validates that entry is editable before publishing.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully published vehicle details for import",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Entry cannot be edited",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/vehicle-details/publish")
    public ResponseEntity<?> publishVehicleDetailsForImport(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid
    ) {
        pdaEntryService.publishVehicleDetailsForImport(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return ApiResponse.success("Vehicle details published for import successfully", null);
    }

    // ==================== TDR Details Operations ====================

    @Operation(
            summary = "Get TDR details",
            description = "Retrieves all TDR details for a PDA entry, ordered by row ID.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved TDR details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryTdrDetailResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/tdr-details")
    public ResponseEntity<?> getTdrDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid
    ) {
        List<PdaEntryTdrDetailResponse> response = pdaEntryService.getTdrDetails(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
        return ApiResponse.success("TDR details retrieved successfully", response);
    }

    @Operation(
            summary = "Bulk save TDR details",
            description = "Bulk save TDR details (create, update, delete in single transaction). " +
                    "For new records, detRowId should be null. For updates, provide existing detRowId. " +
                    "Note: TDR details are typically read-only in legacy. API supports create/update for future enhancements.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully saved TDR details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryTdrDetailResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Entry cannot be edited",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/tdr-details/bulk-save")
    public ResponseEntity<?> bulkSaveTdrDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @Parameter(description = "Bulk save request with TDR details and delete IDs", required = true)
            @Valid @RequestBody BulkSaveTdrDetailsRequest request
    ) {
        List<PdaEntryTdrDetailResponse> response = pdaEntryService.bulkSaveTdrDetails(transactionPoid, request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
        return ApiResponse.success("TDR details saved successfully", response);
    }

    @Operation(
            summary = "Import TDR file",
            description = "Import Excel file containing TDR details using PROC_PDA_IMPORT_TDR_DETAIL2 stored procedure.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully imported TDR file",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid file or import error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping(value = "/{transactionPoid}/tdr-details/import-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importTdrFile(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @Parameter(description = "Excel file containing TDR details", required = true)
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file
    ) {
        String result = pdaEntryService.importTdrFileWithTransaction(file, transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return ApiResponse.success(result, null);
    }

    @Operation(
            summary = "Upload TDR details",
            description = "Upload TDR details for a PDA entry with optional Excel file.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully uploaded TDR details",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/tdr-details/upload")
    public ResponseEntity<?> uploadTdrDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file
    ) {
        String result = pdaEntryService.uploadTdrDetails(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid(), file);
        return ApiResponse.success(result, null);
    }

    @Operation(
            summary = "Clear TDR details",
            description = "Clear all TDR details for a PDA entry using PROC_PDA_TDR_DETAIL_CLEAR stored procedure.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully cleared TDR details",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/tdr-details/clear")
    public ResponseEntity<?> clearTdrDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid
    ) {
        String result = pdaEntryService.clearTdrDetails(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return ApiResponse.success(result, null);
    }

    @Operation(
            summary = "Process TDR charges",
            description = "Process uploaded TDR details and add THC charges to PDA charge detail using PROC_PDA_DEFAULT_CH_FROM_TDR stored procedure.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully processed TDR charges",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/tdr-details/process-charges")
    public ResponseEntity<?> processTdrCharges(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid
    ) {
        String result = pdaEntryService.processTdrCharges(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return ApiResponse.success(result, null);
    }

    // ==================== Acknowledgment Details Operations ====================

    @Operation(
            summary = "Get acknowledgment details",
            description = "Retrieves all acknowledgment details for a PDA entry, ordered by row ID.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved acknowledgment details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryAcknowledgmentDetailResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/acknowledgment-details")
    public ResponseEntity<?> getAcknowledgmentDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid
    ) {
        List<PdaEntryAcknowledgmentDetailResponse> response = pdaEntryService.getAcknowledgmentDetails(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
        return ApiResponse.success("Acknowledgment details retrieved successfully", response);
    }

    @Operation(
            summary = "Bulk save acknowledgment details",
            description = "Bulk save acknowledgment details (create, update, delete in single transaction). " +
                    "For new records, detRowId should be null. For updates, provide existing detRowId. " +
                    "Note: Acknowledgment details are typically read-only in legacy. API supports create/update for future enhancements.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully saved acknowledgment details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaEntryAcknowledgmentDetailResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Entry cannot be edited",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/acknowledgment-details/bulk-save")
    public ResponseEntity<?> bulkSaveAcknowledgmentDetails(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid,
            @Parameter(description = "Bulk save request with acknowledgment details and delete IDs", required = true)
            @Valid @RequestBody BulkSaveAcknowledgmentDetailsRequest request
    ) {
        List<PdaEntryAcknowledgmentDetailResponse> response = pdaEntryService.bulkSaveAcknowledgmentDetails(transactionPoid, request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
        return ApiResponse.success("Acknowledgment details saved successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/acknow/upload-details")
    public ResponseEntity<?> uploadAcknowledgmentDetails(
            @PathVariable Long transactionPoid,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file
    ) {
        String result = pdaEntryService.uploadAcknowledgmentDetailsFromExcel(
                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), 
                UserContext.getUserPoid(), file);
        return ApiResponse.success(result, null);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/acknow/load-details")
    public ResponseEntity<?> loadAcknowledgmentDetails(
            @PathVariable Long transactionPoid
    ) {
        return ApiResponse.success("Acknowledgment details loaded successfully", pdaEntryService.uploadAcknowledgmentDetails(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/acknow/clear-details")
    public ResponseEntity<?> clearAcknowledgmentDetails(
            @PathVariable Long transactionPoid
    ) {
        pdaEntryService.clearAcknowledgmentDetails(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return ApiResponse.success("Acknowledgment details cleared successfully", null);
    }


    // ==================== Special Operations ====================

    @Operation(
            summary = "Validate before save",
            description = "Validates PDA entry before save (called by frontend for real-time validation). " +
                    "Performs all field validations and calls stored procedure for additional validations. " +
                    "Returns validation results with errors and warnings. " +
                    "Transaction POID can be null for new records.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Validation completed",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ValidationResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/validate-before-save")
    public ResponseEntity<?> validateBeforeSave(
            @Parameter(description = "Transaction POID (optional, for existing records)")
            @RequestParam(required = false) Long transactionPoid,
            @Parameter(description = "PDA Entry request to validate", required = true)
            @Valid @RequestBody PdaEntryRequest request
    ) {
        ValidationResponse response = pdaEntryService.validateBeforeSave(transactionPoid, request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return ApiResponse.success("Validation completed", response);
    }

    @Operation(
            summary = "Validate after save",
            description = "Validates PDA entry after save (called after successful save). " +
                    "Calls stored procedure for post-save validation and processing. " +
                    "Returns validation results.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Validation completed",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ValidationResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/{transactionPoid}/validate-after-save")
    public ResponseEntity<?> validateAfterSave(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid
    ) {
        ValidationResponse response = pdaEntryService.validateAfterSave(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return ApiResponse.success("Validation completed", response);
    }

    @Operation(
            summary = "Get vessel details",
            description = "Gets vessel details when vessel LOV is changed (auto-population). " +
                    "Calls stored procedure to retrieve vessel type, IMO number, GRT, NRT, and DWT. " +
                    "Transaction POID is optional (can be null for new records).",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved vessel details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = VesselDetailsResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters - Vessel POID is required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/vessel-details")
    public ResponseEntity<?> getVesselDetails(
            @Parameter(description = "Vessel POID", required = true)
            @RequestParam BigDecimal vesselPoid,
            @Parameter(description = "Transaction POID (optional, for existing records)")
            @RequestParam(required = false) Long transactionPoid
    ) {
        VesselDetailsResponse response = pdaEntryService.getVesselDetails(vesselPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return ApiResponse.success("Vessel details retrieved successfully", response);
    }

    @Operation(
            summary = "Create FDA from PDA",
            description = "Creates an FDA (Freight Disbursement Account) from a PDA entry. " +
                    "Calls PROC_PDA_DTL_UPDATE_FDA stored procedure to create FDA in database. " +
                    "Returns FDA_POID which can be used to redirect to FDA screen. " +
                    "Entry must be in editable state.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully created FDA",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "FDA creation failed",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "PDA entry not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping("/{transactionPoid}/create-fda")
    public ResponseEntity<?> createFda(
            @Parameter(description = "Transaction POID", required = true)
            @PathVariable Long transactionPoid
    ) {
        String result = pdaEntryService.createFda(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        Map<String, String> responseData = new HashMap<>();
        responseData.put("result", result);
        return ApiResponse.success("FDA created successfully", responseData);
    }

    // ==================== FDA Document Operations ====================

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/fda-documents/view")
    public ResponseEntity<?> viewFdaDocument(@PathVariable Long transactionPoid) {
        FdaDocumentViewResponse response = pdaEntryService.getFdaDocumentInfo(
                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
        return ApiResponse.success("FDA document info retrieved successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/fda-documents/accept")
    public ResponseEntity<?> acceptFdaDocuments(@PathVariable Long transactionPoid) {
        pdaEntryService.acceptFdaDocuments(transactionPoid, UserContext.getGroupPoid(), 
                UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return ApiResponse.success("FDA documents accepted successfully", null);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/fda-documents/return")
    public ResponseEntity<?> returnFdaDocuments(
            @PathVariable Long transactionPoid,
            @Valid @RequestBody FdaDocumentReturnRequest request
    ) {
        pdaEntryService.rejectFdaDocs(transactionPoid, UserContext.getGroupPoid(), 
                UserContext.getCompanyPoid(), UserContext.getUserPoid(), request.getCorrectionRemarks());
        return ApiResponse.success("FDA documents returned successfully", null);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/documents/submit-to-accounts")
    public ResponseEntity<?> submitDocumentsToAccounts(@PathVariable Long transactionPoid) {
        pdaEntryService.submitPdaToFda(transactionPoid, UserContext.getGroupPoid(), 
                UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return ApiResponse.success("Documents submitted to accounts successfully", null);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/cancel")
    public ResponseEntity<?> cancelPdaEntry(
            @PathVariable Long transactionPoid,
            @Valid @RequestBody CancelPdaRequest request
    ) {
        pdaEntryService.cancelPdaEntry(transactionPoid, UserContext.getGroupPoid(),
                UserContext.getCompanyPoid(), UserContext.getUserPoid(), request.getCancelRemark());
        return ApiResponse.success("PDA entry cancelled successfully", null);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/submission-log")
    public ResponseEntity<?> getSubmissionLog(@PathVariable Long transactionPoid) {
        SubmissionLogResponse response = pdaEntryService.getSubmissionLogInfo(
                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
        return ApiResponse.success("Submission log info retrieved successfully", response);
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

