package com.asg.operations.projects.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.operations.common.ApiResponse;
import com.asg.operations.projects.dto.*;
import com.asg.operations.projects.service.FFProjectsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/projects/{projectId}/freights")
@Tag(name = "Project Freight Management", description = "APIs for managing freight jobs in projects")
@RequiredArgsConstructor
public class ProjectFreightController {

    private final FFProjectsService projectsService;

    @Operation(summary = "Get all freights summary")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/summary")
    public ResponseEntity<?> getAllFreightsSummary(
            @PathVariable @NotNull Long projectId,
            @ModelAttribute FreightFilterRequest filter) {
        FreightJobsSummaryDTO response = projectsService.getAllFreightsSummary(projectId, filter);
        return ApiResponse.success("Freight summary retrieved successfully", response);
    }

    @Operation(summary = "Get all freights")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/all")
    public ResponseEntity<?> getAllFreights(
            @PathVariable @NotNull Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        List<FreightSummaryDTO> response = projectsService.getAllFreights(projectId, fromDate, toDate, sortBy, sortDir);
        return ApiResponse.success("All freights retrieved successfully", response);
    }

    @Operation(summary = "Get job status and pending bills")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/job-status-pending-bills")
    public ResponseEntity<?> getJobStatusPendingBills(
            @PathVariable @NotNull Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        List<JobStatusPendingBillDTO> response = projectsService.getJobStatusPendingBills(projectId, fromDate, toDate, sortBy, sortDir);
        return ApiResponse.success("Job status and pending bills retrieved successfully", response);
    }

    @Operation(summary = "Get air freight jobs")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/air")
    public ResponseEntity<?> getAirFreights(
            @PathVariable @NotNull Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        List<AirFreightSummaryDTO> response = projectsService.getAirFreightsSummary(projectId, fromDate, toDate, sortBy, sortDir);
        return ApiResponse.success("Air freight jobs retrieved successfully", response);
    }

    @Operation(summary = "Get air freight details")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/air/{jobId}")
    public ResponseEntity<?> getAirFreightDetails(
            @PathVariable @NotNull Long projectId,
            @PathVariable @NotNull Long jobId) {
        AirFreightDetailedDTO response = projectsService.getAirFreightDetails(projectId, jobId);
        return ApiResponse.success("Air freight details retrieved successfully", response);
    }

    @Operation(summary = "Get sea freight jobs")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/sea")
    public ResponseEntity<?> getSeaFreights(
            @PathVariable @NotNull Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        List<SeaFreightSummaryDTO> response = projectsService.getSeaFreightsSummary(projectId, fromDate, toDate, sortBy, sortDir);
        return ApiResponse.success("Sea freight jobs retrieved successfully", response);
    }

    @Operation(summary = "Get sea freight details")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/sea/{jobId}")
    public ResponseEntity<?> getSeaFreightDetails(
            @PathVariable @NotNull Long projectId,
            @PathVariable @NotNull Long jobId) {
        SeaFreightDetailedDTO response = projectsService.getSeaFreightDetails(projectId, jobId);
        return ApiResponse.success("Sea freight details retrieved successfully", response);
    }

    @Operation(summary = "Get road freight jobs")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/road")
    public ResponseEntity<?> getRoadFreights(
            @PathVariable @NotNull Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        List<RoadFreightSummaryDTO> response = projectsService.getRoadFreightsSummary(projectId, fromDate, toDate, sortBy, sortDir);
        return ApiResponse.success("Road freight jobs retrieved successfully", response);
    }

    @Operation(summary = "Get road freight details")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/road/{jobId}")
    public ResponseEntity<?> getRoadFreightDetails(
            @PathVariable @NotNull Long projectId,
            @PathVariable @NotNull Long jobId) {
        RoadFreightDetailedDTO response = projectsService.getRoadFreightDetails(projectId, jobId);
        return ApiResponse.success("Road freight details retrieved successfully", response);
    }

    @Operation(summary = "Get upcoming jobs")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingJobs(
            @PathVariable @NotNull Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        List<UpcomingJobDTO> response = projectsService.getUpcomingJobsList(projectId, fromDate, toDate, sortBy, sortDir);
        return ApiResponse.success("Upcoming jobs retrieved successfully", response);
    }

    @Operation(summary = "Get bayan details by project")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/bayans")
    public ResponseEntity<?> getProjectBayanDetails(
            @PathVariable @NotNull Long projectId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        List<BayanDTO> response = projectsService.getProjectBayanDetails(projectId, sortBy, sortDir);
        return ApiResponse.success("Bayan details retrieved successfully", response);
    }

    @Operation(summary = "Get job charges")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/jobs/{jobId}/charges")
    public ResponseEntity<?> getJobCharges(@PathVariable @NotNull Long jobId) {
        JobChargesDTO response = projectsService.getJobCharges(jobId);
        return ApiResponse.success("Job charges retrieved successfully", response);
    }

    @Operation(summary = "Create job from upcoming")
    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping("/upcoming/{controlSheetDetRowId}/create-job")
    public ResponseEntity<?> createJobFromUpcoming(
            @PathVariable @NotNull Long projectId,
            @PathVariable @NotNull Long controlSheetDetRowId) {
        Long jobId = projectsService.createJobFromUpcoming(projectId, controlSheetDetRowId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job created successfully", jobId));
    }

    @Operation(summary = "Export control sheet to Excel")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel(
            @PathVariable @NotNull Long projectId,
            @RequestParam(required = false) String freightType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        byte[] data = projectsService.exportControlSheetToExcel(projectId, freightType, fromDate, toDate);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "control-sheet.xlsx");
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @Operation(summary = "Export control sheet to PDF")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportToPdf(
            @PathVariable @NotNull Long projectId,
            @RequestParam(required = false) String freightType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        byte[] data = projectsService.exportControlSheetToPdf(projectId, freightType, fromDate, toDate);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "control-sheet.pdf");
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @Operation(summary = "Email control sheet")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/email")
    public ResponseEntity<?> emailControlSheet(
            @PathVariable @NotNull Long projectId,
            @RequestParam @NotNull String emailAddress,
            @RequestParam(required = false) String freightType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        projectsService.emailControlSheet(projectId, emailAddress, freightType, fromDate, toDate);
        return ApiResponse.success("Control sheet emailed successfully");
    }
}
