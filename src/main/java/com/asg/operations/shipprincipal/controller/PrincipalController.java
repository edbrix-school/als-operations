package com.asg.operations.shipprincipal.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.operations.common.ApiResponse;
import com.asg.operations.shipprincipal.dto.*;
import com.asg.operations.shipprincipal.service.PrincipalMasterService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/principal-master")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Principal Management", description = "APIs for managing ship principals")
public class PrincipalController {
    private final PrincipalMasterService principalMasterService;
    private final LoggingService loggingService;

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/search")
    @Operation(
            summary = "Get principal list",
            description = "Retrieve paginated list of principals with optional search and sorting",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> getPrincipalList(
            @RequestBody(required = false) GetAllPrincipalFilterRequest filterRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        if (filterRequest == null) {
            filterRequest = new GetAllPrincipalFilterRequest();
            filterRequest.setIsDeleted("N");
            filterRequest.setOperator("AND");
            filterRequest.setFilters(new java.util.ArrayList<>());
        }

        org.springframework.data.domain.Page<PrincipalListResponse> principalPage = principalMasterService
                .getAllPrincipalsWithFilters(UserContext.getGroupPoid(), filterRequest, page, size, sort);

        java.util.Map<String, String> displayFields = new java.util.HashMap<>();
        displayFields.put("PRINCIPAL_CODE", "text");
        displayFields.put("PRINCIPAL_NAME", "text");

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("content", principalPage.getContent());
        response.put("pageNumber", principalPage.getNumber());
        response.put("displayFields", displayFields);
        response.put("pageSize", principalPage.getSize());
        response.put("totalElements", principalPage.getTotalElements());
        response.put("totalPages", principalPage.getTotalPages());
        response.put("last", principalPage.isLast());

        return ApiResponse.success("Principals retrieved successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{id}")
    @Operation(
            summary = "Get principal details",
            description = "Retrieve full principal information including charges and payments",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved principal",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PrincipalMasterDto.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Principal not found",
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
    public ResponseEntity<?> getPrincipal(
            @Parameter(description = "Principal ID") @PathVariable Long id) {
        log.info("Getting principal with id: {}", id);
        PrincipalMasterDto principal = principalMasterService.getPrincipal(id);
        loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), id.toString());
        log.info("Successfully retrieved principal with id: {}", id);
        return ApiResponse.success("Principal retrieved successfully", principal);
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping
    @Operation(
            summary = "Create principal",
            description = "Create a new principal with charges, payments, and address. Optionally creates GL account.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully created principal",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PrincipalMasterDto.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
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
    public ResponseEntity<?> createPrincipal(
            @Parameter(description = "Principal creation data") @Valid @RequestBody PrincipalCreateDTO dto) {
        log.info("Creating principal with code: {}, groupId: {}, userPoid: {}", dto.getPrincipalCode(), UserContext.getGroupPoid(), UserContext.getUserPoid());
        PrincipalMasterDto result = principalMasterService.createPrincipal(dto, UserContext.getGroupPoid(), UserContext.getUserPoid());
        log.info("Successfully created principal with id: {}", result.getPrincipalPoid());
        return ApiResponse.success("Principal created successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{id}")
    @Operation(
            summary = "Update principal",
            description = "Update principal master and replace charges & payments. Optionally creates GL account if not exists.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated principal",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Principal not found",
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
    public ResponseEntity<?> updatePrincipal(
            @Parameter(description = "Principal ID") @PathVariable Long id,
            @Parameter(description = "Principal update data") @Valid @RequestBody PrincipalUpdateDTO dto) {
        log.info("Updating principal with id: {}, groupId: {}, userPoid: {}", id, UserContext.getGroupPoid(), UserContext.getUserPoid());
        PrincipalMasterDto result = principalMasterService.updatePrincipal(id, dto, UserContext.getGroupPoid(), UserContext.getUserPoid());
        log.info("Successfully updated principal with id: {}", id);
        return ApiResponse.success("Principal updated successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PatchMapping("/{id}/activate")
    @Operation(
            summary = "Toggle active status",
            description = "Toggle the active flag of a principal between Y and N",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully toggled principal status",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Principal not found",
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
    public ResponseEntity<?> toggleActive(
            @Parameter(description = "Principal ID") @PathVariable Long id) {
        log.info("Toggling active status for principal with id: {}", id);
        principalMasterService.toggleActive(id);
        log.info("Successfully toggled active status for principal with id: {}", id);
        return ApiResponse.success("Principal status toggled successfully");
    }

    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete principal",
            description = "Soft delete a principal by setting active flag to N",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully deleted principal",
                            content = @Content(mediaType = "application/json")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Principal not found",
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
    public ResponseEntity<?> deletePrincipal(
            @Parameter(description = "Principal ID") @PathVariable Long id) {
        log.info("Deleting principal with id: {}", id);
        principalMasterService.deletePrincipal(id);
        log.info("Successfully deleted principal with id: {}", id);
        return ApiResponse.success("Principal deleted successfully");
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping("/{id}/create-ledger")
    @Operation(
            summary = "Create GL ledger",
            description = "Create GL ledger account for principal",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> createLedger(
            @Parameter(description = "Principal ID") @PathVariable Long id) {
        log.info("Creating ledger for principal with id: {}, userName: {}", id, UserContext.getUserPoid());
        CreateLedgerResponseDto response = principalMasterService.createLedger(id, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return ApiResponse.success("Ledger created successfully", response);
    }

}
