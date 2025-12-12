package com.asg.operations.crew.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.operations.common.ApiResponse;
import com.asg.operations.crew.dto.*;
import com.asg.operations.crew.service.ContractCrewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Contract Crew Master operations
 */
@RestController
@RequestMapping("/v1/contract-crew-masters")
@Tag(name = "Contract Crew Master", description = "APIs for managing Contract Crew Master records and visa details")
public class ContractCrewController {

    private final ContractCrewService crewService;

    @Autowired
    public ContractCrewController(ContractCrewService crewService) {
        this.crewService = crewService;
    }

    @Operation(summary = "Get all Crew", description = "Returns paginated list of Crew with optional filters. Supports pagination with page and size parameters.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Crew list fetched successfully", content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/search")
    public ResponseEntity<?> getCrewList(
            @RequestBody(required = false) GetAllCrewFilterRequest filterRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {

        // If filterRequest is null, create a default one
        if (filterRequest == null) {
            filterRequest = new GetAllCrewFilterRequest();
            filterRequest.setIsDeleted("N");
            filterRequest.setOperator("AND");
            filterRequest.setFilters(new java.util.ArrayList<>());
        }

        org.springframework.data.domain.Page<ContractCrewResponse> crewPage = crewService
                .getAllCrewWithFilters(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), filterRequest, page, size, sort);

        // Create displayFields
        Map<String, String> displayFields = new HashMap<>();
        displayFields.put("CREW_NAME", "text");
        displayFields.put("NATIONALITY", "text");
        displayFields.put("COMPANY", "text");
        displayFields.put("ACTIVE", "text");
        displayFields.put("CREW_POID", "text");

        // Create paginated response with new structure
        Map<String, Object> response = new HashMap<>();
        response.put("content", crewPage.getContent());
        response.put("pageNumber", crewPage.getNumber());
        response.put("displayFields", displayFields);
        response.put("pageSize", crewPage.getSize());
        response.put("totalElements", crewPage.getTotalElements());
        response.put("totalPages", crewPage.getTotalPages());
        response.put("last", crewPage.isLast());

        return ApiResponse.success("Crew list fetched successfully", response);
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
    @AllowedAction(UserRolesRightsEnum.VIEW)
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
    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping
    public ResponseEntity<?> createCrew(
            @Parameter(description = "Crew master request object with all required and optional fields", required = true)
            @Valid @RequestBody ContractCrewRequest request
    ) {
        ContractCrewResponse response = crewService.createCrew(request, UserContext.getCompanyPoid(), UserContext.getGroupPoid(), UserContext.getUserId());
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
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{crewPoid}")
    public ResponseEntity<?> updateCrew(
            @Parameter(description = "Primary key of the crew master to update", required = true)
            @PathVariable Long crewPoid,
            @Parameter(description = "Crew master request object with updated fields", required = true)
            @Valid @RequestBody ContractCrewRequest request
    ) {
        ContractCrewResponse response = crewService.updateCrew(UserContext.getCompanyPoid(), UserContext.getUserId(), crewPoid, request);
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
    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{crewPoid}")
    public ResponseEntity<?> deleteCrew(
            @Parameter(description = "Primary key of the crew master to delete", required = true)
            @PathVariable Long crewPoid,
            @Parameter(description = "If true, performs hard delete (physical deletion). Default is false (soft delete).")
            @RequestParam(defaultValue = "false") boolean hardDelete
    ) {
        crewService.deleteCrew(UserContext.getCompanyPoid(), crewPoid);
        return ApiResponse.success("Crew master deleted successfully");
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

