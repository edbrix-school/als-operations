package com.asg.operations.projects.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.operations.common.ApiResponse;
import com.asg.operations.projects.dto.FFProjectsCtrlSheetDetailRequest;
import com.asg.operations.projects.dto.FFProjectsCtrlSheetDetailResponse;
import com.asg.operations.projects.dto.FFProjectsRequest;
import com.asg.operations.projects.dto.FFProjectsResponse;
import com.asg.operations.projects.service.FFProjectsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/projects")
@Tag(name = "FF Projects", description = "APIs for managing Freight Forwarding Projects")
@RequiredArgsConstructor
public class FFProjectsController {

    private final FFProjectsService projectsService;
    private final LoggingService loggingService;

    @Operation(
            summary = "Get all Projects",
            description = "Returns paginated list of Projects with optional filters",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Projects list fetched successfully",
                            content = @Content(schema = @Schema(implementation = Page.class))
                    )
            }
    )
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/list")
    public ResponseEntity<?> getProjectsList(
            @RequestBody(required = false) FilterRequestDto filterRequest,
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) LocalDate periodFrom,
            @RequestParam(required = false) LocalDate periodTo) {
        Map<String, Object> projectsPage = projectsService.listProjectsWithFilters(
                UserContext.getDocumentId(),
                filterRequest,
                pageable,
                periodFrom,
                periodTo
        );
        return ApiResponse.success("Projects list fetched successfully", projectsPage);
    }

    @Operation(summary = "Get Project by ID", description = "Retrieve a specific project by transaction POID")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}")
    public ResponseEntity<?> getProjectById(@PathVariable @NotNull Long transactionPoid) {
        FFProjectsResponse response = projectsService.getProjectById(transactionPoid);
        loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), transactionPoid.toString());
        return ApiResponse.success("Project retrieved successfully", response);
    }

    @Operation(summary = "Create new Project", description = "Create a new freight forwarding project")
    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping
    public ResponseEntity<?> createProject(@Valid @RequestBody FFProjectsRequest request) {
        FFProjectsResponse response = projectsService.createProject(request);
        return ApiResponse.success("Project created successfully", response);
    }

    @Operation(summary = "Update Project", description = "Update an existing project")
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{transactionPoid}")
    public ResponseEntity<?> updateProject(
            @PathVariable @NotNull Long transactionPoid,
            @Valid @RequestBody FFProjectsRequest request) {
        FFProjectsResponse response = projectsService.updateProject(transactionPoid, request);
        return ApiResponse.success("Project updated successfully", response);
    }

    @Operation(summary = "Delete Project", description = "Soft delete a project")
    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{transactionPoid}")
    public ResponseEntity<?> deleteProject(
            @PathVariable @NotNull Long transactionPoid,
            @Valid @RequestBody(required = false) DeleteReasonDto deleteReasonDto) {
        projectsService.deleteProject(transactionPoid, deleteReasonDto);
        return ApiResponse.success("Project deleted successfully");
    }

    @Operation(summary = "Load Quotation Details", description = "Load quotation details for project creation")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/load-quotation/{quotationPoid}")
    public ResponseEntity<?> loadQuotationDetails(
            @PathVariable @NotNull Long quotationPoid,
            @RequestParam(value = "quoteFlag", defaultValue = "N") String quoteFlag) {
        Map<String, Object> result = projectsService.loadQuotationDetails(quotationPoid, quoteFlag);
        return ApiResponse.success("Quotation details loaded successfully", result);
    }

    @Operation(summary = "Load Job Details", description = "Load job details for project control sheet")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/load-jobs/{transactionPoid}")
    public ResponseEntity<?> loadJobDetails(@PathVariable @NotNull Long transactionPoid) {
        Map<String, Object> result = projectsService.loadJobDetails(transactionPoid);
        return ApiResponse.success("Job details loaded successfully", result);
    }

    // Control Sheet Batch Operations API

    @Operation(summary = "Create Control Sheet", description = "Create a new control sheet for a project")
    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping("/{transactionPoid}/control-sheets")
    public ResponseEntity<?> createControlSheet(
            @PathVariable @NotNull Long transactionPoid,
            @Valid @RequestBody FFProjectsCtrlSheetDetailRequest request) {
        FFProjectsCtrlSheetDetailResponse response = projectsService.createControlSheet(transactionPoid, request);
        return ApiResponse.success("Control sheet created successfully", response);
    }

    @Operation(summary = "Update Control Sheet", description = "Update an existing control sheet row for a project")
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{transactionPoid}/control-sheets/{detRowId}")
    public ResponseEntity<?> updateControlSheet(
            @PathVariable @NotNull Long transactionPoid,
            @PathVariable @NotNull Long detRowId,
            @Valid @RequestBody FFProjectsCtrlSheetDetailRequest request) {
        FFProjectsCtrlSheetDetailResponse response = projectsService.updateControlSheet(transactionPoid, detRowId, request);
        return ApiResponse.success("Control sheet updated successfully", response);
    }

    @Operation(summary = "Batch Control Sheet Operations", description = "Create, update, or delete multiple control sheet entries based on actionType")
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/control-sheets/batch")
    public ResponseEntity<?> batchControlSheetOperations(
            @PathVariable @NotNull Long transactionPoid,
            @Valid @RequestBody List<FFProjectsCtrlSheetDetailRequest> requests) {
        List<FFProjectsCtrlSheetDetailResponse> response = projectsService.batchControlSheetOperations(transactionPoid, requests);
        return ApiResponse.success("Control sheet operations completed successfully", response);
    }

    @Operation(summary = "Get Control Sheets by Project", description = "Retrieve all control sheets for a project, optionally filtered by freight type")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/control-sheets")
    public ResponseEntity<?> getControlSheetsByProject(
            @PathVariable @NotNull Long transactionPoid,
            @RequestParam(required = false) String freightType) {
        List<FFProjectsCtrlSheetDetailResponse> response = projectsService.getControlSheetsByProject(transactionPoid, freightType);
        return ApiResponse.success("Control sheets retrieved successfully", response);
    }

    @Operation(summary = "Toggle Control Sheet Active", description = "Toggle active/inactive status of a control sheet row. Rows linked to a job number cannot be made inactive.")
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PatchMapping("/{transactionPoid}/control-sheets/{detRowId}/toggle-active")
    public ResponseEntity<?> toggleControlSheetActive(
            @PathVariable @NotNull Long transactionPoid,
            @PathVariable @NotNull Long detRowId,
            @RequestParam @NotNull String active) {
        FFProjectsCtrlSheetDetailResponse response = projectsService.toggleControlSheetActive(transactionPoid, detRowId, active);
        return ApiResponse.success("Control sheet status updated successfully", response);
    }

    @Operation(
            summary = "Upload Control Sheet from Excel",
            description = "Upload an Excel file to replace all control sheet entries for a project. " +
                    "Pre-validates for duplicate rows and LOV field values. " +
                    "Fails if any existing control sheet row is linked to a job."
    )
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping(value = "/{transactionPoid}/control-sheets/upload-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadControlSheetExcel(
            @PathVariable @NotNull Long transactionPoid,
            @RequestParam("file") MultipartFile file) {
        List<FFProjectsCtrlSheetDetailResponse> response = projectsService.uploadControlSheetExcel(transactionPoid, file);
        return ApiResponse.success("Control sheet uploaded successfully", response);
    }
}
