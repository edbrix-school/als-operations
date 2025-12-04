package com.asg.operations.portcallreport.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.operations.common.ApiResponse;
import com.asg.operations.portcallreport.dto.PortActivityResponseDto;
import com.asg.operations.portcallreport.dto.PortCallReportDto;
import com.asg.operations.portcallreport.dto.PortCallReportResponseDto;
import com.asg.operations.portcallreport.service.PortCallReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * @param page page number (0-based)
     * @param size page size
     * @param sort sort field and direction
     * @param search search term for filtering
     * @return paginated list of port call reports
     */
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping
    @Operation(
            summary = "Get port call report list",
            description = "Retrieve paginated list of port call reports with optional search and sorting",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> getReportList(
            @Parameter(description = "Page number (0-based)") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(required = false, defaultValue = "20") int size,
            @Parameter(description = "Sort field and direction (e.g., 'portCallReportId,asc')") @RequestParam(required = false) String sort,
            @Parameter(description = "Search term for filtering") @RequestParam(required = false) String search) {
        try {
            Sort sortObj = Sort.by(Sort.Direction.ASC, "portCallReportId");
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
            Page<PortCallReportResponseDto> reports = portCallReportService.getReportList(search, pageable);
            return ApiResponse.success("Reports retrieved successfully", reports);
        } catch (Exception e) {
            log.error("Error fetching report list", e);
            return ApiResponse.internalServerError("Error fetching reports: " + e.getMessage());
        }
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
        try {
            PortCallReportResponseDto report = portCallReportService.getReportById(id);
            if (report == null) {
                return ApiResponse.notFound("Report not found");
            }
            return ApiResponse.success("Report retrieved successfully", report);
        } catch (Exception e) {
            log.error("Error fetching report by id: {}", id, e);
            return ApiResponse.internalServerError("Error fetching report: " + e.getMessage());
        }
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
        try {
            PortCallReportResponseDto created = portCallReportService.createReport(dto, UserContext.getUserPoid(), UserContext.getGroupPoid());
            return ApiResponse.success("Report created successfully", created);
        } catch (Exception e) {
            log.error("Error creating report", e);
            return ApiResponse.internalServerError("Error creating report: " + e.getMessage());
        }
    }

    /**
     * Updates an existing port call report.
     *
     * @param id report ID
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
        try {
            PortCallReportResponseDto updated = portCallReportService.updateReport(id, dto, UserContext.getUserPoid(), UserContext.getGroupPoid());
            return ApiResponse.success("Report updated successfully", updated);
        } catch (Exception e) {
            log.error("Error updating report id: {}", id, e);
            return ApiResponse.internalServerError("Error updating report: " + e.getMessage());
        }
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
        try {
            portCallReportService.deleteReport(id);
            return ApiResponse.success("Report deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting report id: {}", id, e);
            return ApiResponse.internalServerError("Error deleting report: " + e.getMessage());
        }
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
        try {
            List<PortActivityResponseDto> activities = portCallReportService.getPortActivities(UserContext.getUserPoid());
            return ApiResponse.success("Activities retrieved successfully", activities);
        } catch (Exception e) {
            log.error("Error fetching port activities", e);
            return ApiResponse.internalServerError("Error fetching activities: " + e.getMessage());
        }
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
        try {
            List<Map<String, Object>> vesselTypes = portCallReportService.getVesselTypes();
            return ApiResponse.success("Vessel types retrieved successfully", vesselTypes);
        } catch (Exception e) {
            log.error("Error fetching vessel types", e);
            return ApiResponse.internalServerError("Error fetching vessel types: " + e.getMessage());
        }
    }
}
