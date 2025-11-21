package com.alsharif.operations.crew.controller;


import com.alsharif.operations.crew.dto.*;
import com.alsharif.operations.crew.service.ContractCrewService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Contract Crew Master operations
 */
@RestController
@RequestMapping("/api/v1/contract-crew-masters")
@Tag(name = "Contract Crew Master", description = "APIs for managing Contract Crew Master records and visa details")
public class ContractCrewController {

    private final ContractCrewService crewService;

    @Autowired
    public ContractCrewController(ContractCrewService crewService) {
        this.crewService = crewService;
    }

    /**
     * GET /api/v1/contract-crew-masters
     * Get paginated list of crew masters with filters
     */
    @Operation(
            summary = "Get crew master list",
            description = "Retrieves a paginated list of crew masters with optional filtering and sorting. " +
                    "Supports filtering by crew code, crew name, nationality, company, and active status. " +
                    "Results are paginated and can be sorted by any field. " +
                    "Only records accessible to the user's company are returned (multi-tenant filtering).",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved crew master list",
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
    public ResponseEntity<PageResponse<ContractCrewResponse>> getCrewList(
            @RequestHeader("companyPoid") Long companyPoid,
            @RequestParam(required = false) String crewName,
            @RequestParam(required = false) Long nationality,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "crewName,asc") String sort
    ) {
        // Parse sort parameter (format: "field,direction")
        Sort sortObj = parseSort(sort);

        // Create pageable
        Pageable pageable = PageRequest.of(page, size, sortObj);

        // Call service
        PageResponse<ContractCrewResponse> response = crewService.getCrewList(
                crewName,
                nationality,
                company,
                active,
                pageable,
                companyPoid
        );

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/contract-crew-masters/{crewPoid}
     * Get crew master by ID
     */
    @Operation(
            summary = "Get crew master by ID",
            description = "Retrieves a single crew master record by its primary key (crewPoid). " +
                    "Includes nationality code and name from the NATIONALITY master. " +
                    "Returns 404 if crew master is not found or not accessible to the user's company.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved crew master",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ContractCrewResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Crew master not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - User doesn't have access to this record",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{crewPoid}")
    public ResponseEntity<ContractCrewResponse> getCrewById(
            @Parameter(description = "Primary key of the crew master", required = true)
            @PathVariable Long crewPoid) {
        ContractCrewResponse response = crewService.getCrewById(crewPoid);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/contract-crew-masters
     * Create new crew master
     */
    @Operation(
            summary = "Create crew master",
            description = "Creates a new crew master record with all required and optional fields. " +
                    "Validates all mandatory fields, passport dates (expiry must be after issue date), " +
                    "and verifies nationality exists in the system. " +
                    "Crew code is auto-generated using sequence or business rules. " +
                    "Audit fields (created by, created date, company POID, group POID) are automatically populated. " +
                    "Default value for active status is 'Y' if not provided.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Successfully created crew master",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ContractCrewResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Nationality not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict - Crew code already exists",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<ContractCrewResponse> createCrew(
            @Parameter(description = "Crew master request object with all required and optional fields", required = true)
            @Valid @RequestBody ContractCrewRequest request,
            @RequestHeader("companyPoid") Long companyPoid,
            @RequestHeader("groupPoid") Long groupPoid,
            @RequestHeader("userId") String userId
    ) {
        ContractCrewResponse response = crewService.createCrew(request, companyPoid, groupPoid,userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/v1/contract-crew-masters/{crewPoid}
     * Update existing crew master
     */
    @Operation(
            summary = "Update crew master",
            description = "Updates an existing crew master record. All fields except crew code and crew POID can be updated. " +
                    "Validates all mandatory fields, passport dates, and verifies nationality exists. " +
                    "Audit fields (modified by, modified date) are automatically updated. " +
                    "Created by and created date remain unchanged. " +
                    "Returns 404 if crew master is not found or not accessible to the user's company.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated crew master",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ContractCrewResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - User doesn't have permission to update this record",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Crew master not found",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{crewPoid}")
    public ResponseEntity<ContractCrewResponse> updateCrew(
            @RequestHeader("companyPoid") Long companyPoid,
            @Parameter(description = "Primary key of the crew master to update", required = true)
            @PathVariable Long crewPoid,
            @Parameter(description = "Crew master request object with updated fields", required = true)
            @Valid @RequestBody ContractCrewRequest request
    ) {
        ContractCrewResponse response = crewService.updateCrew(companyPoid,crewPoid, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/v1/contract-crew-masters/{crewPoid}
     * Delete crew master (soft or hard delete)
     */
    @Operation(
            summary = "Delete crew master",
            description = "Deletes a crew master record. Supports both soft delete (default) and hard delete. " +
                    "Soft delete (hardDelete=false): Sets ACTIVE='N' and updates audit fields. All detail records remain intact. " +
                    "Hard delete (hardDelete=true): Physically deletes all detail records first (cascade), then deletes the master record. " +
                    "Returns 404 if crew master is not found. Returns 409 if crew is referenced in other records (if business rules exist).",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully deleted crew master",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - User doesn't have permission to delete",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Crew master not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict - Cannot delete crew referenced in other records",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{crewPoid}")
    public ResponseEntity<Map<String, Object>> deleteCrew(
            @RequestHeader("companyPoid") Long companyPoid,
            @Parameter(description = "Primary key of the crew master to delete", required = true)
            @PathVariable Long crewPoid,
            @Parameter(description = "If true, performs hard delete (physical deletion). Default is false (soft delete).")
            @RequestParam(defaultValue = "false") boolean hardDelete
    ) {
        crewService.deleteCrew(companyPoid,crewPoid);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Crew master deleted successfully");
        response.put("crewPoid", crewPoid);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/contract-crew-masters/{crewPoid}/details
     * Get crew details list
     */
    @Operation(
            summary = "Get crew details (visa details)",
            description = "Retrieves all visa/document details for a specific crew master. " +
                    "Details are returned ordered by detRowId. " +
                    "Each detail record includes document type code and name from the CREW_VISA_TYPE master. " +
                    "Returns 404 if crew master is not found.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved crew details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CrewDetailsResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Crew master not found",
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
    @GetMapping("/{crewPoid}/details")
    public ResponseEntity<CrewDetailsResponse> getCrewDetails(
            @RequestHeader("companyPoid") Long companyPoid,
            @Parameter(description = "Primary key of the crew master", required = true)
            @PathVariable Long crewPoid) {
        CrewDetailsResponse response = crewService.getCrewDetails(companyPoid,crewPoid);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/contract-crew-masters/{crewPoid}/details
     * Bulk save crew details
     */
    @Operation(
            summary = "Bulk save crew details",
            description = "Saves all crew detail (visa) records in a single transaction. Supports insert, update, and delete operations. " +
                    "This API consolidates multiple legacy operations into one bulk operation for better performance and transaction integrity. " +
                    "Validates all detail records including date validations (expiry after issue, issue after applied). " +
                    "Validates document types exist in CREW_VISA_TYPE master. " +
                    "Operations are processed in order: deletions first, then updates, then inserts. " +
                    "All operations are executed within a single database transaction. " +
                    "If any validation fails, the entire operation is rolled back.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully saved crew details",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Crew master not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict - Detail row not found for UPDATE operations",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{crewPoid}/details")
    public ResponseEntity<Map<String, Object>> saveCrewDetails(
            @RequestHeader("companyPoid") Long companyPoid,
            @RequestHeader("userId") String userId,
            @Parameter(description = "Primary key of the crew master", required = true)
            @PathVariable Long crewPoid,
            @Parameter(description = "Bulk save request containing details list and deleted row IDs", required = true)
            @Valid @RequestBody BulkSaveDetailsRequest request
    ) {
        CrewDetailsResponse response = crewService.saveCrewDetails(companyPoid,userId,crewPoid, request);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Details saved successfully");
        result.put("crewPoid", response.getCrewPoid());
        result.put("savedDetails", response.getDetails());

        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/v1/contract-crew-masters/{crewPoid}/details/{detRowId}
     * Delete single crew detail record
     */
    @Operation(
            summary = "Delete single crew detail",
            description = "Deletes a single crew detail (visa) record by its detRowId. " +
                    "This is an alternative to the bulk save API for single-row deletion. " +
                    "Returns 404 if crew master or detail record is not found.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully deleted detail record",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Crew master or detail record not found",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{crewPoid}/details/{detRowId}")
    public ResponseEntity<Map<String, Object>> deleteCrewDetail(
            @RequestHeader("companyPoid") Long companyPoid,

            @Parameter(description = "Primary key of the crew master", required = true)
            @PathVariable Long crewPoid,
            @Parameter(description = "Detail row ID to delete", required = true)
            @PathVariable Long detRowId
    ) {
        crewService.deleteCrewDetail(companyPoid,crewPoid, detRowId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Detail record deleted successfully");
        response.put("crewPoid", crewPoid);
        response.put("detRowId", detRowId);

        return ResponseEntity.ok(response);
    }

    /**
     * Parse sort parameter string into Sort object
     * Format: "field,direction" (e.g., "crewCode,asc" or "crewName,desc")
     */
    private Sort parseSort(String sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.by(Sort.Direction.ASC, "crewCode");
        }

        String[] parts = sort.split(",");
        if (parts.length != 2) {
            return Sort.by(Sort.Direction.ASC, "crewCode");
        }

        String field = parts[0].trim();
        String direction = parts[1].trim().toLowerCase();

        Sort.Direction sortDirection = "desc".equals(direction) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;

        return Sort.by(sortDirection, field);
    }
}

