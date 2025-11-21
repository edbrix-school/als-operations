package com.alsharif.operations.commonlov.controller;

import com.alsharif.operations.commonlov.dto.PdaRateTypeRequestDTO;
import com.alsharif.operations.commonlov.dto.PdaRateTypeResponseDTO;
import com.alsharif.operations.commonlov.service.PdaRateTypeServiceImpl;
import com.alsharif.operations.exceptions.ResourceNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.alsharif.operations.common.ApiResponse.internalServerError;
import static com.alsharif.operations.common.ApiResponse.success;

@RestController
@RequestMapping("/api/v1/pda-rate-type")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class PdaRateTypeController {

    private final PdaRateTypeServiceImpl service;

    // ==================================================
    // GET LIST
    // ==================================================
    @Operation(
            summary = "Get PDA rate type list",
            description = "Retrieves a paginated list of PDA rate types with optional filtering and sorting. " +
                    "Supports filtering by rate type code, rate type name, and active status. " +
                    "Results are paginated and can be sorted by any field. " +
                    "By default, returns only active records (active='Y') and excludes deleted records.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved rate type list",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class)
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
    public ResponseEntity<?> getRateTypeList(
            @Parameter(description = "Filter by rate type code (partial match, case-insensitive)")
            @RequestParam(required = false) String code,
            @Parameter(description = "Filter by rate type name (partial match, case-insensitive)")
            @RequestParam(required = false) String name,
            @Parameter(description = "Filter by active status ('Y' or 'N'). Default is 'Y'.")
            @RequestParam(required = false) String active,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field and direction (e.g., 'rateTypeCode,asc' or 'seqno,desc')")
            @RequestParam(defaultValue = "rateTypeCode,asc") String sort
    ) {
        try {
            Sort sortObj = parseSort(sort);
            Pageable pageable = PageRequest.of(page, size, sortObj);
            
            Page<PdaRateTypeResponseDTO> response = service.getRateTypeList(
                    code,
                    name,
                    active,
                    pageable
            );
            
            return success("Rate type list retrieved successfully", response);
        } catch (Exception ex) {
            return internalServerError("Failed to retrieve rate type list: " + ex.getMessage());
        }
    }

    private Sort parseSort(String sort) {
        String[] parts = sort.split(",");
        String field = parts[0];
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1]) 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }

    // ==================================================
    // CREATE
    // ==================================================
    @Operation(
            summary = "Create a new PDA Rate Type",
            description = "Creates a new PDA Rate Type Master record",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Successfully created the PDA Rate Type",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PdaRateTypeResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Validation error"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @PostMapping
    public ResponseEntity<?> create(
            @Valid @org.springframework.web.bind.annotation.RequestBody
            PdaRateTypeRequestDTO requestDTO,

            @Parameter(description = "Document identifier", example = "PDA-RT-001")
            @RequestParam String documentId,

            @Parameter(description = "Action requested", example = "create")
            @RequestParam String actionRequested
    ) {
        try {
            PdaRateTypeResponseDTO response = service.createRateType(requestDTO);
            return success("PDA Rate Type created successfully", response);

        } catch (Exception ex) {
            return internalServerError("Failed to create PDA Rate Type: " + ex.getMessage());
        }
    }

    // ==================================================
    // UPDATE
    // ==================================================
    @Operation(
            summary = "Update an existing PDA Rate Type",
            description = "Updates the PDA Rate Type Master using POID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated",
                            content = @Content(schema = @Schema(implementation = PdaRateTypeResponseDTO.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Record not found"),
                    @ApiResponse(responseCode = "400", description = "Validation error")
            }
    )
    @PutMapping("/{rateTypePoid}")
    public ResponseEntity<?> update(
            @PathVariable Long rateTypePoid,

            @Valid @org.springframework.web.bind.annotation.RequestBody
            PdaRateTypeRequestDTO requestDTO,

            @RequestParam String documentId,
            @RequestParam String actionRequested
    ) {
        try {
            PdaRateTypeResponseDTO response = service.updateRateType(rateTypePoid, requestDTO);
            return success("PDA Rate Type updated successfully", response);

        } catch (ResourceNotFoundException ex) {
            return internalServerError(ex.getMessage());

        } catch (Exception ex) {
            return internalServerError("Failed to update PDA Rate Type: " + ex.getMessage());
        }
    }

    // ==================================================
    // GET BY ID
    // ==================================================
    @Operation(
            summary = "Get PDA Rate Type by POID",
            description = "Fetch PDA Rate Type details using its POID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully fetched",
                            content = @Content(schema = @Schema(implementation = PdaRateTypeResponseDTO.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found")
            }
    )
    @GetMapping("/{rateTypePoid}")
    public ResponseEntity<?> getById(
            @PathVariable Long rateTypePoid,
            @RequestParam String documentId,
            @RequestParam String actionRequested
    ) {
        try {
            PdaRateTypeResponseDTO dto = service.getRateTypeById(rateTypePoid);
            return success("PDA Rate Type fetched successfully", dto);

        } catch (ResourceNotFoundException ex) {
            return internalServerError(ex.getMessage());
        }
    }

    // ==================================================
    // DELETE (SOFT DELETE)
    // ==================================================
    @Operation(
            summary = "Soft delete a PDA Rate Type",
            description = "Marks a PDA Rate Type as deleted without removing data",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully deleted"),
                    @ApiResponse(responseCode = "404", description = "Record not found")
            }
    )
    @DeleteMapping("/{rateTypePoid}")
    public ResponseEntity<?> delete(
            @PathVariable Long rateTypePoid,
            @RequestParam String documentId,
            @RequestParam String actionRequested
    ) {
        try {
            service.deleteRateType(rateTypePoid, false); // soft delete
            return success("PDA Rate Type soft deleted successfully");

        } catch (ResourceNotFoundException ex) {
            return internalServerError(ex.getMessage());
        }
    }


    // ==================================================
// VALIDATE FORMULA
// ==================================================
    @Operation(
            summary = "Validate PDA Rate Type Formula",
            description = "Validates formula syntax, allowed tokens, brackets, and operators used in PDA Rate Type.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Formula validated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = com.alsharif.operations.commonlov.dto.FormulaValidationResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid formula")
            }
    )
    @PostMapping("/validate-formula")
    public ResponseEntity<?> validateFormula(
            @Valid @org.springframework.web.bind.annotation.RequestBody
            com.alsharif.operations.commonlov.dto.FormulaValidationRequest request,

            @RequestParam String documentId,
            @RequestParam String actionRequested
    ) {
        try {
            var response = service.validateFormula(request);
            return success("Formula validated successfully", response);

        } catch (Exception ex) {
            return internalServerError("Formula validation failed: " + ex.getMessage());
        }
    }

}
