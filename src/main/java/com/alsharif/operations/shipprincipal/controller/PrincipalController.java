package com.alsharif.operations.shipprincipal.controller;

import com.alsharif.operations.common.ApiResponse;
import com.alsharif.operations.shipprincipal.dto.*;
import com.alsharif.operations.shipprincipal.service.PrincipalService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/principals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Principal Management", description = "APIs for managing ship principals")
public class PrincipalController {
    private final PrincipalService principalService;

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
                                    schema = @Schema(implementation = PrincipalDetailDTO.class)
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
            @Parameter(description = "Principal ID") @PathVariable Long id,
            @Parameter(description = "Document ID from request context", required = true) @RequestHeader("X-Document-Id") Long documentId,
            @Parameter(description = "User POID from request context", required = true) @RequestHeader("X-User-Poid") Long userPoid) {
        log.info("Getting principal with id: {}, documentId: {}, userPoid: {}", id, documentId, userPoid);
        try {
            PrincipalDetailDTO principal = principalService.getPrincipal(id);
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
                                    schema = @Schema(implementation = Long.class)
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
            @Parameter(description = "Principal creation data") @RequestBody PrincipalCreateDTO dto,
            @Parameter(description = "Document ID from request context", required = true) @RequestHeader("X-Document-Id") Long documentId,
            @Parameter(description = "User POID from request context", required = true) @RequestHeader("X-User-Poid") Long userPoid) {
        log.info("Creating principal with code: {}, documentId: {}, userPoid: {}", dto.getPrincipalCode(), documentId, userPoid);
        try {
            Long id = principalService.createPrincipal(dto);
            log.info("Successfully created principal with id: {}", id);
            return ApiResponse.success("Principal created successfully", id);
        } catch (RuntimeException e) {
            log.error("Bad request while creating principal: {}", dto.getPrincipalCode(), e);
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating principal: {}", dto.getPrincipalCode(), e);
            return ApiResponse.internalServerError("Error creating principal: " + e.getMessage());
        }
    }

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
            @Parameter(description = "Principal update data") @RequestBody PrincipalUpdateDTO dto,
            @Parameter(description = "Document ID from request context", required = true) @RequestHeader("X-Document-Id") Long documentId,
            @Parameter(description = "User POID from request context", required = true) @RequestHeader("X-User-Poid") Long userPoid) {
        log.info("Updating principal with id: {}, documentId: {}, userPoid: {}", id, documentId, userPoid);
        try {
            principalService.updatePrincipal(id, dto);
            log.info("Successfully updated principal with id: {}", id);
            return ApiResponse.success("Principal updated successfully");
        } catch (RuntimeException e) {
            log.error("Principal not found with id: {}", id, e);
            return ApiResponse.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating principal with id: {}", id, e);
            return ApiResponse.internalServerError("Error updating principal: " + e.getMessage());
        }
    }

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
            @Parameter(description = "Principal ID") @PathVariable Long id,
            @Parameter(description = "Document ID from request context", required = true) @RequestHeader("X-Document-Id") Long documentId,
            @Parameter(description = "User POID from request context", required = true) @RequestHeader("X-User-Poid") Long userPoid) {
        log.info("Toggling active status for principal with id: {}, documentId: {}, userPoid: {}", id, documentId, userPoid);
        try {
            principalService.toggleActive(id);
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
            @Parameter(description = "Principal ID") @PathVariable Long id,
            @Parameter(description = "Document ID from request context", required = true) @RequestHeader("X-Document-Id") Long documentId,
            @Parameter(description = "User POID from request context", required = true) @RequestHeader("X-User-Poid") Long userPoid) {
        log.info("Deleting principal with id: {}, documentId: {}, userPoid: {}", id, documentId, userPoid);
        try {
            principalService.deletePrincipal(id);
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
}
