package com.asg.operations.projectjob.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import static com.asg.common.lib.dto.response.ApiResponse.success;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.operations.common.ApiResponse;
import com.asg.operations.commonlov.dto.LovItem;
import com.asg.operations.projectjob.dto.ProjectJobRequest;
import com.asg.operations.projectjob.dto.ProjectJobResponse;
import com.asg.operations.projectjob.dto.ProjectLoadInJobsProcResponse;
import com.asg.operations.projectjob.service.ProjectJobService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for PDA Entry Form operations
 */
@RestController
@RequestMapping("/v1/project-job")
@Tag(name = "Project Job", description = "APIs for managing Project Job forms and related details")
@RequiredArgsConstructor
@Slf4j
public class ProjectJobController {

    private final ProjectJobService projectJobService;
    private final LoggingService loggingService;

    @Operation(summary = "Get Project Job by ID", description = "Retrieves a Project by transaction POID. ", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved Project Job", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectJobResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project Job not found", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json"))}, security = @SecurityRequirement(name = "bearerAuth"))
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}")
    public ResponseEntity<?> getProjectJobById(
            @Parameter(description = "Transaction POID", required = true) @PathVariable Long transactionPoid) {
        ProjectJobResponse response = projectJobService.getById(transactionPoid);
        loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(),
                transactionPoid.toString());
        return ApiResponse.success("Project job retrieved successfully", response);
    }

    @Operation(summary = "Create project Job", description = "Creates a new project Job with information. ", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully created Project Job", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectJobResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input parameters or validation error", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json"))}, security = @SecurityRequirement(name = "bearerAuth"))
    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping
    public ResponseEntity<?> createProjectJob(
            @Parameter(description = "Project JOb request", required = true) @Valid @RequestBody ProjectJobRequest request) {
        ProjectJobResponse response = projectJobService.create(request);
        return ApiResponse.success("project Job created successfully", response);
    }

    @Operation(summary = "Update Project Job", description = "Updates an existing Project JOb.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully updated Project Job", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectJobResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input parameters or validation error", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Edit not allowed for this entry", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project Job not found", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json"))}, security = @SecurityRequirement(name = "bearerAuth"))
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{transactionPoid}")
    public ResponseEntity<?> updateProjectJob(
            @Parameter(description = "Transaction POID", required = true) @PathVariable Long transactionPoid,
            @Parameter(description = "Project Job request", required = true) @Valid @RequestBody ProjectJobRequest request) {
        ProjectJobResponse response = projectJobService.update(transactionPoid, request);
        return ApiResponse.success("Project Job updated successfully", response);
    }

    @Operation(summary = "Reopen Project JOb by id", description = "Reopen Project JOb by transaction POID.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully reopened Project Job", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "project Job not found", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json"))}, security = @SecurityRequirement(name = "bearerAuth"))
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/reopen-job/{transactionPoid}")
    public ResponseEntity<?> reopenJobById(
            @Parameter(description = "Transaction POID", required = true) @PathVariable Long transactionPoid) {
        String response = projectJobService.reopenJob(transactionPoid);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Load Project by ID", description = "Load Project by transaction POID.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully loads Project", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectLoadInJobsProcResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json"))}, security = @SecurityRequirement(name = "bearerAuth"))
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/load-job/{transactionPoid}")
    public ResponseEntity<?> loadJobById(
            @Parameter(description = "Transaction POID", required = true) @PathVariable Long transactionPoid) {
        ProjectLoadInJobsProcResponse response = projectJobService.loadJobs(transactionPoid);
        return ApiResponse.success("Project loaded successfully", response);
    }

    @Operation(summary = "Delete Project JOb", description = "Soft deletes a Project Job by setting DELETED = 'Y'.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully deleted Project JOb", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Deletion not allowed for this entry", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project Job not found", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json"))}, security = @SecurityRequirement(name = "bearerAuth"))
    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{transactionPoid}")
    public ResponseEntity<?> deleteProjectJob(
            @Parameter(description = "Transaction POID", required = true) @PathVariable Long transactionPoid,
            @Valid @RequestBody(required = false) DeleteReasonDto deleteReasonDto) {
        projectJobService.deleteById(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(),
                UserContext.getUserPoid(), deleteReasonDto);
        return ApiResponse.success("Project Job deleted successfully");
    }

    @Operation(summary = "Get Project Jobs list", description = "Retrieves a paginated list of Project Jobs with optional filtering and sorting. "
            + "Results are paginated and can be sorted by any field. "
            + "Only records accessible to the user's company are returned (multi-tenant filtering).", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved Project list", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))}, security = @SecurityRequirement(name = "bearerAuth"))
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/search")
    public ResponseEntity<?> getProjectJobList(@RequestBody(required = false) FilterRequestDto filterRequest,
                                               @ParameterObject Pageable pageable, @RequestParam(required = false) LocalDate periodFrom,
                                               @RequestParam(required = false) LocalDate periodTo) {
        Map<String, Object> page = projectJobService.getAllProjectJobsWithFilters(UserContext.getDocumentId(),
                filterRequest, pageable, periodFrom, periodTo);
        return success("Project Jobs retrieved successfully", page);
    }

    @Operation(summary = "Get Notify value by poid", description = "Retrieves a Notify details by NotifyPOID. ", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved Notify details"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notify details not found", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json"))}, security = @SecurityRequirement(name = "bearerAuth"))
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/notify/{notifyPoid}")
    public ResponseEntity<?> getNotifyById(
            @Parameter(description = "Notify POID", required = true) @PathVariable BigDecimal notifyPoid) {
        LovItem response = projectJobService.getNotifyById(notifyPoid);
        return ApiResponse.success("Notify details retrieved successfully", response);
    }

}
