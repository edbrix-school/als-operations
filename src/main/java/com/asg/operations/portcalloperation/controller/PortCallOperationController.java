package com.asg.operations.portcalloperation.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.operations.portcalloperation.dto.PortCallOperationCreateDto;
import com.asg.operations.portcalloperation.dto.PortCallOperationDto;
import com.asg.operations.portcalloperation.dto.PortCallOperationEstBertDetailDto;
import com.asg.operations.portcalloperation.dto.PortCallOperationEstBertDetailResponseDto;
import com.asg.operations.portcalloperation.dto.PortCallOperationEstPrearrivalActDetailDto;
import com.asg.operations.portcalloperation.dto.PortCallOperationEstPrearrivalActDetailResponseDto;
import com.asg.operations.portcalloperation.dto.PortCallOperationActTimingsActvtyDetailDto;
import com.asg.operations.portcalloperation.dto.PortCallOperationActTimingsActvtyDetailResponseDto;
import com.asg.operations.portcalloperation.dto.PortCallOperationDocsCopyDetailDto;
import com.asg.operations.portcalloperation.dto.PortCallOperationDocsCopyDetailResponseDto;
import com.asg.operations.portcalloperation.dto.PortCallOperationResponseDto;
import com.asg.operations.portcalloperation.service.PortCallOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.notFound;
import static com.asg.common.lib.dto.response.ApiResponse.success;

/**
 * Controller for managing port call operations.
 * Provides REST endpoints for CRUD operations on port call operations.
 */
@RestController
@RequestMapping("/v1/port-call-operations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Port Call Operation", description = "APIs for managing port call operations")
public class PortCallOperationController {

    private final PortCallOperationService portCallOperationService;
    private final LoggingService loggingService;

    /**
     * Retrieves paginated list of port call operations.
     *
     * @return paginated list of port call operations
     */
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/list")
    @Operation(
            summary = "Get port call operation list",
            description = "Retrieve paginated list of port call operations with optional search and sorting",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> listOperations(@ParameterObject Pageable pageable,
                                            @RequestBody(required = false) FilterRequestDto filters,
                                            @RequestParam(required = false) LocalDate startDate,
                                            @RequestParam(required = false) LocalDate endDate) {
        return success("Operations retrieved successfully", portCallOperationService.listOperations(UserContext.getDocumentId(), filters, pageable, startDate, endDate));
    }

    /**
     * Retrieves port call operation by ID.
     *
     * @param id operation ID
     * @return port call operation details
     */
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{id}")
    @Operation(
            summary = "Get port call operation by ID",
            description = "Retrieve port call operation details including activities",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> getOperationById(@Parameter(description = "Operation ID") @PathVariable Long id) {

        PortCallOperationResponseDto operation = portCallOperationService.getOperationById(id);
        if (operation == null) {
            return notFound("Operation not found");
        }
        loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), id.toString());
        return success("Operation retrieved successfully", operation);
    }

    /**
     * Creates a new port call operation.
     *
     * @param dto port call operation data
     * @return created port call operation
     */
    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping
    @Operation(
            summary = "Create port call operation",
            description = "Create a new port call operation",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> createOperation(@Valid @RequestBody PortCallOperationCreateDto dto) {
        PortCallOperationResponseDto created = portCallOperationService.createOperation(dto, UserContext.getUserPoid(), UserContext.getGroupPoid());
        return success("Operation created successfully", created);
    }

    /**
     * Updates an existing port call operation.
     *
     * @param id  operation ID
     * @param dto port call operation data
     * @return updated port call operation
     */
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{id}")
    @Operation(
            summary = "Update port call operation",
            description = "Update an existing port call operation",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> updateOperation(@Parameter(description = "Operation ID") @PathVariable Long id,
                                             @Valid @RequestBody PortCallOperationDto dto) {
        PortCallOperationResponseDto updated = portCallOperationService.updateOperation(id, dto, UserContext.getUserPoid(), UserContext.getGroupPoid());
        return success("Operation updated successfully", updated);
    }

    /**
     * Deletes a port call operation.
     *
     * @param id operation ID
     * @param deleteReasonDto delete reason details
     * @return success response
     */
    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete port call operation",
            description = "Delete a port call operation",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> deleteOperation(@Parameter(description = "Operation ID") @PathVariable Long id,
                                             @Valid @RequestBody DeleteReasonDto deleteReasonDto) {
        portCallOperationService.deleteOperation(id, deleteReasonDto);
        return success("Operation deleted successfully");
    }

    // Stored Procedure Endpoints
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/load-pda/{pdaPoid}")
    @Operation(
            summary = "Load PDA data",
            description = "Load PDA Header and Detail data to be displayed on the screen",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> loadPda(@Parameter(description = "PDA POID") @PathVariable String pdaPoid) {
        Map<String, Object> result = portCallOperationService.loadPda(pdaPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return success("PDA data loaded successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/load-fda/{fdaPoid}")
    @Operation(
            summary = "Load FDA data",
            description = "Load FDA Header and Detail data to be displayed on the screen",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> loadFda(@Parameter(description = "FDA POID") @PathVariable String fdaPoid) {
        Map<String, Object> result = portCallOperationService.loadFda(fdaPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return success("FDA data loaded successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/load-voyage/{voyagePoid}")
    @Operation(
            summary = "Load Voyage data",
            description = "Load Vessel Voyage Detail data to be displayed on the screen",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> loadVoyage(@Parameter(description = "Voyage POID") @PathVariable Long voyagePoid) {
        Map<String, Object> result = portCallOperationService.loadVoyage(voyagePoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return success("Voyage data loaded successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/load-email-list/{transactionPoid}")
    @Operation(
            summary = "Load Email List",
            description = "List down the email details added in Port Call Information - Other details tab",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> loadEmailList(@Parameter(description = "Transaction POID") @PathVariable String transactionPoid) {
        Map<String, Object> result = portCallOperationService.loadEmailList(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return success("Email list loaded successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/mail-template/{transactionPoid}/{templatePoid}")
    @Operation(
            summary = "Get Mail Template",
            description = "Get mail template with port call header, cargo details, activity details, and email template",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> getMailTemplate(@Parameter(description = "Transaction POID") @PathVariable String transactionPoid,
                                             @Parameter(description = "Template POID") @PathVariable Long templatePoid) {
        Map<String, Object> result = portCallOperationService.getMailTemplate(transactionPoid, templatePoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return success("Mail template retrieved successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/port-report-activities/{transactionPoid}/{portReportPoid}")
    @Operation(
            summary = "Get Port Report Activities",
            description = "List down the activities based on the passed port activity report poid",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> getPortReportActivities(@Parameter(description = "Transaction POID") @PathVariable String transactionPoid,
                                                     @Parameter(description = "Port Report POID") @PathVariable Long portReportPoid) {
        Map<String, Object> result = portCallOperationService.getPortReportActivities(transactionPoid, portReportPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return success("Port report activities retrieved successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/email-record/{emailPoid}/{transactionPoid}")
    @Operation(
            summary = "Get Email Record",
            description = "Retrieve the email details sent to be used in various screen for preview purpose",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> getEmailRecord(@Parameter(description = "Email POID") @PathVariable Long emailPoid,
                                            @Parameter(description = "Transaction POID") @PathVariable String transactionPoid) {
        Map<String, Object> result = portCallOperationService.getEmailRecord(emailPoid, transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return success("Email record retrieved successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/email-history/{transactionPoid}")
    @Operation(
            summary = "Get Email History",
            description = "Retrieve the list of email sent for the selected port call",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> getEmailHistory(@Parameter(description = "Transaction POID") @PathVariable String transactionPoid) {
        Map<String, Object> result = portCallOperationService.getEmailHistory(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return success("Email history retrieved successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/est-bert-details/{detRowId}")
    @Operation(
            summary = "Get EstBertDetail",
            description = "Retrieve a specific EstBertDetail by transaction and detail row ID",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> getEstBertDetail(@Parameter(description = "Transaction POID") @PathVariable Long transactionPoid,
                                              @Parameter(description = "Detail Row ID") @PathVariable Long detRowId) {
        PortCallOperationEstBertDetailResponseDto result = portCallOperationService.getEstBertDetail(transactionPoid, detRowId);
        return success("EstBertDetail retrieved successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping("/{transactionPoid}/est-bert-details")
    @Operation(
            summary = "Create EstBertDetail",
            description = "Create a new EstBertDetail for a port call operation",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> createEstBertDetail(@Parameter(description = "Transaction POID") @PathVariable Long transactionPoid,
                                                  @Valid @RequestBody PortCallOperationEstBertDetailDto dto) {
        PortCallOperationResponseDto result = portCallOperationService.createEstBertDetail(transactionPoid, dto);
        return success("EstBertDetail created successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{transactionPoid}/est-bert-details/{detRowId}")
    @Operation(
            summary = "Update EstBertDetail",
            description = "Update an existing EstBertDetail for a port call operation",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> updateEstBertDetail(@Parameter(description = "Transaction POID") @PathVariable Long transactionPoid,
                                                  @Parameter(description = "Detail Row ID") @PathVariable Long detRowId,
                                                  @Valid @RequestBody PortCallOperationEstBertDetailDto dto) {
        PortCallOperationResponseDto result = portCallOperationService.updateEstBertDetail(transactionPoid, detRowId, dto);
        return success("EstBertDetail updated successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/est-prearrival-details/{detRowId}/activities")
    @Operation(
            summary = "List EstPrearrivalActDetails",
            description = "Retrieve list of activities for a specific prearrival detail",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> listEstPrearrivalActDetails(@Parameter(description = "Transaction POID") @PathVariable Long transactionPoid,
                                                          @Parameter(description = "Detail Row ID") @PathVariable Long detRowId) {
        List<PortCallOperationEstPrearrivalActDetailResponseDto> result = portCallOperationService.listEstPrearrivalActDetails(transactionPoid, detRowId);
        return success("EstPrearrivalActDetails retrieved successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping("/{transactionPoid}/est-prearrival-details/{detRowId}/activities")
    @Operation(
            summary = "Create EstPrearrivalActDetail",
            description = "Create a new activity for a prearrival detail",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> createEstPrearrivalActDetail(@Parameter(description = "Transaction POID") @PathVariable Long transactionPoid,
                                                           @Parameter(description = "Detail Row ID") @PathVariable Long detRowId,
                                                           @Valid @RequestBody PortCallOperationEstPrearrivalActDetailDto dto) {
        PortCallOperationEstPrearrivalActDetailResponseDto result = portCallOperationService.createEstPrearrivalActDetail(transactionPoid, detRowId, dto);
        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, UserContext.getDocumentId(), transactionPoid.toString());
        return success("EstPrearrivalActDetail created successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{transactionPoid}/est-prearrival-details/{detRowId}/activities/{preActivityDtlPoid}")
    @Operation(
            summary = "Update EstPrearrivalActDetail",
            description = "Update an existing activity for a prearrival detail",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> updateEstPrearrivalActDetail(@Parameter(description = "Transaction POID") @PathVariable Long transactionPoid,
                                                           @Parameter(description = "Detail Row ID") @PathVariable Long detRowId,
                                                           @Parameter(description = "Pre Activity Detail POID") @PathVariable Long preActivityDtlPoid,
                                                           @Valid @RequestBody PortCallOperationEstPrearrivalActDetailDto dto) {
        PortCallOperationEstPrearrivalActDetailResponseDto result = portCallOperationService.updateEstPrearrivalActDetail(transactionPoid, detRowId, preActivityDtlPoid, dto);
        loggingService.createLogSummaryEntry(LogDetailsEnum.MODIFIED, UserContext.getDocumentId(), transactionPoid.toString());
        return success("EstPrearrivalActDetail updated successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/act-timing-details/{detRowId}/activities")
    @Operation(
            summary = "List ActTimingsActvtyDetails",
            description = "Retrieve list of activities for a specific actual timing detail",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> listActTimingsActvtyDetails(@Parameter(description = "Transaction POID") @PathVariable Long transactionPoid,
                                                          @Parameter(description = "Detail Row ID") @PathVariable Long detRowId) {
        List<PortCallOperationActTimingsActvtyDetailResponseDto> result = portCallOperationService.listActTimingsActvtyDetails(transactionPoid, detRowId);
        return success("ActTimingsActvtyDetails retrieved successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping("/{transactionPoid}/act-timing-details/{detRowId}/activities")
    @Operation(
            summary = "Create ActTimingsActvtyDetail",
            description = "Create a new activity for an actual timing detail",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> createActTimingsActvtyDetail(@Parameter(description = "Transaction POID") @PathVariable Long transactionPoid,
                                                           @Parameter(description = "Detail Row ID") @PathVariable Long detRowId,
                                                           @Valid @RequestBody PortCallOperationActTimingsActvtyDetailDto dto) {
        PortCallOperationActTimingsActvtyDetailResponseDto result = portCallOperationService.createActTimingsActvtyDetail(transactionPoid, detRowId, dto);
        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, UserContext.getDocumentId(), transactionPoid.toString());
        return success("ActTimingsActvtyDetail created successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{transactionPoid}/act-timing-details/{detRowId}/activities/{actualsTimingDtlPoid}")
    @Operation(
            summary = "Update ActTimingsActvtyDetail",
            description = "Update an existing activity for an actual timing detail",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> updateActTimingsActvtyDetail(@Parameter(description = "Transaction POID") @PathVariable Long transactionPoid,
                                                           @Parameter(description = "Detail Row ID") @PathVariable Long detRowId,
                                                           @Parameter(description = "Actuals Timing Detail POID") @PathVariable Long actualsTimingDtlPoid,
                                                           @Valid @RequestBody PortCallOperationActTimingsActvtyDetailDto dto) {
        PortCallOperationActTimingsActvtyDetailResponseDto result = portCallOperationService.updateActTimingsActvtyDetail(transactionPoid, detRowId, actualsTimingDtlPoid, dto);
        loggingService.createLogSummaryEntry(LogDetailsEnum.MODIFIED, UserContext.getDocumentId(), transactionPoid.toString());
        return success("ActTimingsActvtyDetail updated successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/docs-copy-details/{detRowId}")
    @Operation(
            summary = "Get DocsCopyDetail",
            description = "Retrieve a specific DocsCopyDetail by transaction and detail row ID",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> getDocsCopyDetail(@Parameter(description = "Transaction POID") @PathVariable Long transactionPoid,
                                               @Parameter(description = "Detail Row ID") @PathVariable Long detRowId) {
        PortCallOperationDocsCopyDetailResponseDto result = portCallOperationService.getDocsCopyDetail(transactionPoid, detRowId);
        loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), transactionPoid.toString());
        return success("DocsCopyDetail retrieved successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping("/{transactionPoid}/docs-copy-details")
    @Operation(
            summary = "Create DocsCopyDetail",
            description = "Create a new DocsCopyDetail for a port call operation",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> createDocsCopyDetail(@Parameter(description = "Transaction POID") @PathVariable Long transactionPoid,
                                                  @Valid @RequestBody PortCallOperationDocsCopyDetailDto dto) {
        PortCallOperationResponseDto result = portCallOperationService.createDocsCopyDetail(transactionPoid, dto);
        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, UserContext.getDocumentId(), transactionPoid.toString());
        return success("DocsCopyDetail created successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{transactionPoid}/docs-copy-details/{detRowId}")
    @Operation(
            summary = "Update DocsCopyDetail",
            description = "Update an existing DocsCopyDetail for a port call operation",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> updateDocsCopyDetail(@Parameter(description = "Transaction POID") @PathVariable Long transactionPoid,
                                                  @Parameter(description = "Detail Row ID") @PathVariable Long detRowId,
                                                  @Valid @RequestBody PortCallOperationDocsCopyDetailDto dto) {
        PortCallOperationResponseDto result = portCallOperationService.updateDocsCopyDetail(transactionPoid, detRowId, dto);
        loggingService.createLogSummaryEntry(LogDetailsEnum.MODIFIED, UserContext.getDocumentId(), transactionPoid.toString());
        return success("DocsCopyDetail updated successfully", result);
    }
}
