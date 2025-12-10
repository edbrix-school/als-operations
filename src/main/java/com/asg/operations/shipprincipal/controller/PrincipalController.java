package com.asg.operations.shipprincipal.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/principal-master")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Principal Management", description = "APIs for managing ship principals")
public class PrincipalController {
    private final PrincipalMasterService principalMasterService;

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/list")
    @Operation(
            summary = "Get principal list",
            description = "Retrieve paginated list of principals with optional search and sorting",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> getPrincipalList(
            @Parameter(description = "Page number (0-based)") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(required = false, defaultValue = "20") int size,
            @Parameter(description = "Sort field and direction (e.g., 'principalCode,asc')") @RequestParam(required = false) String sort,
            @Parameter(description = "Search term for filtering") @RequestParam(required = false) String search) {

        Sort sortObj = Sort.by(Sort.Direction.ASC, "principalCode");
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                Sort.Direction direction = sortParts[1].equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
                sortObj = Sort.by(direction, sortParts[0]);
            }
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<PrincipalMasterListDto> result = principalMasterService.getPrincipalList(search, pageable);
        return ApiResponse.success("Principal list retrieved successfully", result);
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
        try {
            PrincipalMasterDto principal = principalMasterService.getPrincipal(id);
            log.info("Successfully retrieved principal with id: {}", id);
            return ApiResponse.success("Principal retrieved successfully", principal);
        } catch (RuntimeException e) {
            log.error("Principal not found with id: {}", id, e);
            return ApiResponse.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving principal with id: {}", id, e);
            return ApiResponse.internalServerError("Error retrieving principal: " + e.getMessage());
        }
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
        try {
            PrincipalMasterDto result = principalMasterService.createPrincipal(dto, UserContext.getGroupPoid(), UserContext.getUserPoid());
            log.info("Successfully created principal with id: {}", result.getPrincipalPoid());
            return ApiResponse.success("Principal created successfully", result);
        } catch (RuntimeException e) {
            log.error("Bad request while creating principal: {}", dto.getPrincipalCode(), e);
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating principal: {}", dto.getPrincipalCode(), e);
            return ApiResponse.internalServerError("Error creating principal: " + e.getMessage());
        }
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
        try {
            PrincipalMasterDto result = principalMasterService.updatePrincipal(id, dto, UserContext.getGroupPoid(), UserContext.getUserPoid());
            log.info("Successfully updated principal with id: {}", id);
            return ApiResponse.success("Principal updated successfully", result);
        } catch (RuntimeException e) {
            log.error("Principal not found with id: {}", id, e);
            return ApiResponse.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating principal with id: {}", id, e);
            return ApiResponse.internalServerError("Error updating principal: " + e.getMessage());
        }
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
        try {
            principalMasterService.toggleActive(id);
            log.info("Successfully toggled active status for principal with id: {}", id);
            return ApiResponse.success("Principal status toggled successfully");
        } catch (RuntimeException e) {
            log.error("Principal not found with id: {}", id, e);
            return ApiResponse.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("Error toggling principal status with id: {}", id, e);
            return ApiResponse.internalServerError("Error toggling principal status: " + e.getMessage());
        }
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
        try {
            principalMasterService.deletePrincipal(id);
            log.info("Successfully deleted principal with id: {}", id);
            return ApiResponse.success("Principal deleted successfully");
        } catch (RuntimeException e) {
            log.error("Principal not found with id: {}", id, e);
            return ApiResponse.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting principal with id: {}", id, e);
            return ApiResponse.internalServerError("Error deleting principal: " + e.getMessage());
        }
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
        try {
            CreateLedgerResponseDto response = principalMasterService.createLedger(id, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
            return ApiResponse.success("Ledger created successfully", response);
        } catch (RuntimeException e) {
            log.error("Error creating ledger for principal: {}", id, e);
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating ledger for principal: {}", id, e);
            return ApiResponse.internalServerError("Error creating ledger: " + e.getMessage());
        }
    }

}
