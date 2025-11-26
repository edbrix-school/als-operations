package com.alsharif.operations.crew.controller;


import com.alsharif.operations.common.ApiResponse;
import com.alsharif.operations.crew.dto.*;
import com.alsharif.operations.crew.service.ContractCrewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved crew master list",
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
    @GetMapping
    public ResponseEntity<?> getCrewList(
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

        return ApiResponse.success("Crew list retrieved successfully", response);
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
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved crew master",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ContractCrewResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Crew master not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - User doesn't have access to this record",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{crewPoid}")
    public ResponseEntity<?> getCrewById(
            @Parameter(description = "Primary key of the crew master", required = true)
            @PathVariable Long crewPoid) {
        ContractCrewResponse response = crewService.getCrewById(crewPoid);
        return ApiResponse.success("Crew retrieved successfully", response);
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
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "201",
                            description = "Successfully created crew master",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ContractCrewResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Nationality not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "Conflict - Crew code already exists",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<?> createCrew(
            @Parameter(description = "Crew master request object with all required and optional fields", required = true)
            @Valid @RequestBody ContractCrewRequest request,
            @RequestHeader("companyPoid") Long companyPoid,
            @RequestHeader("groupPoid") Long groupPoid,
            @RequestHeader("userId") String userId
    ) {
        ContractCrewResponse response = crewService.createCrew(request, companyPoid, groupPoid,userId);
        return ApiResponse.success("Crew created successfully", response);
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
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated crew master",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ContractCrewResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - User doesn't have permission to update this record",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Crew master not found",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{crewPoid}")
    public ResponseEntity<?> updateCrew(
            @RequestHeader("companyPoid") Long companyPoid,
            @RequestHeader("userId") String userPoid,
            @Parameter(description = "Primary key of the crew master to update", required = true)
            @PathVariable Long crewPoid,
            @Parameter(description = "Crew master request object with updated fields", required = true)
            @Valid @RequestBody ContractCrewRequest request
    ) {
        ContractCrewResponse response = crewService.updateCrew(companyPoid,userPoid,crewPoid, request);
        return ApiResponse.success("Crew updated successfully", response);
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
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully deleted crew master",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - User doesn't have permission to delete",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Crew master not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "Conflict - Cannot delete crew referenced in other records",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{crewPoid}")
    public ResponseEntity<?> deleteCrew(
            @RequestHeader("companyPoid") Long companyPoid,
            @Parameter(description = "Primary key of the crew master to delete", required = true)
            @PathVariable Long crewPoid,
            @Parameter(description = "If true, performs hard delete (physical deletion). Default is false (soft delete).")
            @RequestParam(defaultValue = "false") boolean hardDelete
    ) {
        crewService.deleteCrew(companyPoid,crewPoid);
        return ApiResponse.success("Crew master deleted successfully");
    }

//    /**
//     * GET /api/v1/contract-crew-masters/{crewPoid}/details
//     * Get crew details list
//     * @deprecated This endpoint is deprecated. Use GET /api/v1/contract-crew-masters/{crewPoid} instead.
//     * The main GET API now includes child details in the response.
//     */
//    @Deprecated
//    @Operation(
//            summary = "Get crew details (visa details) - DEPRECATED",
//            description = "⚠ DEPRECATED: Use GET /api/v1/contract-crew-masters/{crewPoid} instead. " +
//                    "The main GET API now includes child details in the response. " +
//                    "This endpoint is kept for backward compatibility only.",
//            responses = {
//                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                            responseCode = "200",
//                            description = "Successfully retrieved crew details",
//                            content = @Content(
//                                    mediaType = "application/json",
//                                    schema = @Schema(implementation = CrewDetailsResponse.class)
//                            )
//                    ),
//                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                            responseCode = "404",
//                            description = "Crew master not found",
//                            content = @Content(mediaType = "application/json")
//                    ),
//                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                            responseCode = "401",
//                            description = "Unauthorized - Authentication required",
//                            content = @Content(mediaType = "application/json")
//                    )
//            },
//            security = @SecurityRequirement(name = "bearerAuth")
//    )
//    @GetMapping("/{crewPoid}/details")
//    public ResponseEntity<?> getCrewDetails(
//            @RequestHeader("companyPoid") Long companyPoid,
//            @Parameter(description = "Primary key of the crew master", required = true)
//            @PathVariable Long crewPoid) {
//        CrewDetailsResponse response = crewService.getCrewDetails(companyPoid,crewPoid);
//        return ApiResponse.success("Crew details retrieved successfully", response);
//    }

/*
    *//**
     * POST /api/v1/contract-crew-masters/{crewPoid}/details
     * Bulk save crew details
     *//*
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
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully saved crew details",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Crew master not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "Conflict - Detail row not found for UPDATE operations",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{crewPoid}/details")
    public ResponseEntity<?> saveCrewDetails(
            @RequestHeader("companyPoid") Long companyPoid,
            @RequestHeader("userId") String userId,
            @Parameter(description = "Primary key of the crew master", required = true)
            @PathVariable Long crewPoid,
            @Parameter(description = "Bulk save request containing details list and deleted row IDs", required = true)
            @Valid @RequestBody BulkSaveDetailsRequest request
    ) {
        CrewDetailsResponse response = crewService.saveCrewDetails(companyPoid,userId,crewPoid, request);
        return ApiResponse.success("Crew details saved successfully", response);
    }

  */  /**
     * DELETE /api/v1/contract-crew-masters/{crewPoid}/details/{detRowId}
     * Delete single crew detail record
     */
    @Operation(
            summary = "Delete single crew detail",
            description = "Deletes a single crew detail (visa) record by its detRowId. " +
                    "This is an alternative to the bulk save API for single-row deletion. " +
                    "Returns 404 if crew master or detail record is not found.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully deleted detail record",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Crew master or detail record not found",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{crewPoid}/details/{detRowId}")
    public ResponseEntity<?> deleteCrewDetail(
            @RequestHeader("companyPoid") Long companyPoid,

            @Parameter(description = "Primary key of the crew master", required = true)
            @PathVariable Long crewPoid,
            @Parameter(description = "Detail row ID to delete", required = true)
            @PathVariable Long detRowId
    ) {
        crewService.deleteCrewDetail(companyPoid,crewPoid, detRowId);
        return ApiResponse.success("Crew detail deleted successfully");
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

