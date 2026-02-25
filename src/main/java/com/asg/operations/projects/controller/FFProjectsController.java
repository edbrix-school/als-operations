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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> loadQuotationDetails(@PathVariable @NotNull Long quotationPoid) {
        Map<String, Object> result = projectsService.loadQuotationDetails(quotationPoid);
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

    @Operation(summary = "Get Air Freight Jobs", description = "Retrieve air freight jobs for a project")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/freight-jobs/air")
    public ResponseEntity<?> getAirFreightJobs(@PathVariable @NotNull Long transactionPoid) {
        List<?> response = projectsService.getAirFreightJobs(transactionPoid);
        return ApiResponse.success("Air freight jobs retrieved successfully", response);
    }

    @Operation(summary = "Get Sea Freight Jobs", description = "Retrieve sea freight jobs for a project")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/freight-jobs/sea")
    public ResponseEntity<?> getSeaFreightJobs(@PathVariable @NotNull Long transactionPoid) {
        List<?> response = projectsService.getSeaFreightJobs(transactionPoid);
        return ApiResponse.success("Sea freight jobs retrieved successfully", response);
    }

    @Operation(summary = "Get Road Freight Jobs", description = "Retrieve road freight jobs for a project")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/freight-jobs/road")
    public ResponseEntity<?> getRoadFreightJobs(@PathVariable @NotNull Long transactionPoid) {
        List<?> response = projectsService.getRoadFreightJobs(transactionPoid);
        return ApiResponse.success("Road freight jobs retrieved successfully", response);
    }

    @Operation(summary = "Get All Freight Jobs", description = "Retrieve all freight jobs for a project with optional date range")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/freight-jobs")
    public ResponseEntity<?> getAllFreightJobs(
            @PathVariable @NotNull Long transactionPoid,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {
        List<?> response = projectsService.getAllFreightJobs(transactionPoid, fromDate, toDate);
        return ApiResponse.success("All freight jobs retrieved successfully", response);
    }
}
