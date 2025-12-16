package com.asg.operations.portcallreport.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.operations.common.ApiResponse;
import com.asg.operations.portcallreport.dto.GetAllPortCallReportFilterRequest;
import com.asg.operations.portcallreport.dto.PortActivityResponseDto;
import com.asg.operations.portcallreport.dto.PortCallReportDto;
import com.asg.operations.portcallreport.dto.PortCallReportListResponse;
import com.asg.operations.portcallreport.dto.PortCallReportResponseDto;
import com.asg.operations.portcallreport.service.PortCallReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing port call reports.
 * Provides REST endpoints for CRUD operations on port call reports.
 */
@RestController
@RequestMapping("/v1/port-call-reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Port Call Report", description = "APIs for managing port call reports")
public class PortCallReportController {

    private final PortCallReportService portCallReportService;

    /**
     * Retrieves paginated list of port call reports.
     *
     * @param page   page number (0-based)
     * @param size   page size
     * @param sort   sort field and direction
     * @return paginated list of port call reports
     */
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/search")
    @Operation(
            summary = "Get port call report list",
            description = "Retrieve paginated list of port call reports with optional search and sorting",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> getReportList(
            @RequestBody(required = false) GetAllPortCallReportFilterRequest filterRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        if (filterRequest == null) {
            filterRequest = new GetAllPortCallReportFilterRequest();
            filterRequest.setIsDeleted("N");
            filterRequest.setOperator("AND");
            filterRequest.setFilters(new java.util.ArrayList<>());
        }

        org.springframework.data.domain.Page<PortCallReportListResponse> reportPage = portCallReportService
                .getAllPortCallReportsWithFilters(UserContext.getGroupPoid(), filterRequest, page, size, sort);

        Map<String, String> displayFields = new LinkedHashMap<>();
        displayFields.put("PORT_CALL_REPORT_ID", "text");
        displayFields.put("PORT_CALL_REPORT_NAME", "text");

        Map<String, Object> response = new HashMap<>();
        response.put("content", reportPage.getContent());
        response.put("pageNumber", reportPage.getNumber());
        response.put("displayFields", displayFields);
        response.put("pageSize", reportPage.getSize());
        response.put("totalElements", reportPage.getTotalElements());
        response.put("totalPages", reportPage.getTotalPages());
        response.put("last", reportPage.isLast());

        return ApiResponse.success("Reports retrieved successfully", response);
    }

    /**
     * Retrieves port call report by ID.
     *
     * @param id report ID
     * @return port call report details
     */
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{id}")
    @Operation(
            summary = "Get port call report by ID",
            description = "Retrieve port call report details including activities",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> getReportById(@Parameter(description = "Report ID") @PathVariable Long id) {

        PortCallReportResponseDto report = portCallReportService.getReportById(id);
        if (report == null) {
            return ApiResponse.notFound("Report not found");
        }
        return ApiResponse.success("Report retrieved successfully", report);
    }

    /**
     * Creates a new port call report.
     *
     * @param dto port call report data
     * @return created port call report
     */
    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping
    @Operation(
            summary = "Create port call report",
            description = "Create a new port call report",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> createReport(
            @Valid @RequestBody PortCallReportDto dto) {
        PortCallReportResponseDto created = portCallReportService.createReport(dto, UserContext.getUserPoid(), UserContext.getGroupPoid());
        return ApiResponse.success("Report created successfully", created);
    }

    /**
     * Updates an existing port call report.
     *
     * @param id  report ID
     * @param dto port call report data
     * @return updated port call report
     */
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{id}")
    @Operation(
            summary = "Update port call report",
            description = "Update an existing port call report",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> updateReport(
            @Parameter(description = "Report ID") @PathVariable Long id,
            @Valid @RequestBody PortCallReportDto dto) {
        PortCallReportResponseDto updated = portCallReportService.updateReport(id, dto, UserContext.getUserPoid(), UserContext.getGroupPoid());
        return ApiResponse.success("Report updated successfully", updated);
    }

    /**
     * Deletes a port call report.
     *
     * @param id report ID
     * @return success response
     */
    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete port call report",
            description = "Delete a port call report",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> deleteReport(@Parameter(description = "Report ID") @PathVariable Long id) {
        portCallReportService.deleteReport(id);
        return ApiResponse.success("Report deleted successfully");
    }

    /**
     * Retrieves list of port activities.
     *
     * @return list of port activities
     */
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/port-activities")
    @Operation(
            summary = "Get port activities",
            description = "Load list of port activities",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> getPortActivities() {
        List<PortActivityResponseDto> activities = portCallReportService.getPortActivities(UserContext.getUserPoid());
        return ApiResponse.success("Activities retrieved successfully", activities);
    }

    /**
     * Retrieves list of vessel types.
     *
     * @return list of vessel types
     */
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/vessel-types")
    @Operation(
            summary = "Get vessel types",
            description = "Load list of vessel types for LOV",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> getVesselTypes() {
        List<Map<String, Object>> vesselTypes = portCallReportService.getVesselTypes();
        return ApiResponse.success("Vessel types retrieved successfully", vesselTypes);
    }
}
