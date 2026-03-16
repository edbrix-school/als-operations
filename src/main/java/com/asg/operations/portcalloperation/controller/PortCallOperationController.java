package com.asg.operations.portcalloperation.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.excel.ExcelFileData;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.ExcelExportService;
import com.asg.common.lib.service.LoggingService;
import com.asg.operations.portcalloperation.dto.*;
import com.asg.operations.portcalloperation.service.PortCallOperationPcInfoAttachmentService;
import com.asg.operations.portcalloperation.service.PortCallOperationScreenAttachmentService;
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
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.*;

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
    private final PortCallOperationPcInfoAttachmentService pcInfoAttachmentService;
    private final PortCallOperationScreenAttachmentService screenAttachmentService;
    private final ExcelExportService excelExportService;

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
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Update port call operation",
            description = "Update an existing port call operation. Supports uploading husbandry crew and other attachments via multipart form-data.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> updateOperation(@Parameter(description = "Operation ID") @PathVariable Long id,
                                             @Valid @RequestPart("dto") PortCallOperationDto dto,
                                             @RequestPart(value = "husbandryCrewFiles", required = false) MultipartFile[] husbandryCrewFiles,
                                             @RequestPart(value = "husbandryCrewDetRowId", required = false) Long husbandryCrewDetRowId,
                                             @RequestPart(value = "husbandryCrewRemarks", required = false) String[] husbandryCrewRemarks,
                                             @RequestPart(value = "husbandryCrewChecklistName", required = false) String[] husbandryCrewChecklistNames,
                                             @RequestPart(value = "husbandryOthFiles", required = false) MultipartFile[] husbandryOthFiles,
                                             @RequestPart(value = "husbandryOthDetRowId", required = false) Long husbandryOthDetRowId,
                                             @RequestPart(value = "husbandryOthRemarks", required = false) String[] husbandryOthRemarks,
                                             @RequestPart(value = "husbandryOthChecklistName", required = false) String[] husbandryOthChecklistNames) {

        PortCallOperationResponseDto updated = portCallOperationService.updateOperation(id, dto, UserContext.getUserPoid(), UserContext.getGroupPoid());

        // Optionally handle husbandry attachments in the same call
        if ((husbandryCrewFiles != null && husbandryCrewFiles.length > 0) || (husbandryOthFiles != null && husbandryOthFiles.length > 0)) {
            if (requireAttachmentService() != null) {
                return requireAttachmentService();
            }

            if (husbandryCrewFiles != null && husbandryCrewFiles.length > 0 && husbandryCrewDetRowId != null) {
                screenAttachmentService.uploadHusbandryCrewAttachments(id, husbandryCrewDetRowId, husbandryCrewFiles, husbandryCrewRemarks, husbandryCrewChecklistNames);
            }

            if (husbandryOthFiles != null && husbandryOthFiles.length > 0 && husbandryOthDetRowId != null) {
                screenAttachmentService.uploadHusbandryOthAttachments(id, husbandryOthDetRowId, husbandryOthFiles, husbandryOthRemarks, husbandryOthChecklistNames);
            }
        }

        return success("Operation updated successfully", updated);
    }

    /**
     * Deletes a port call operation.
     *
     * @param id              operation ID
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
        loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), transactionPoid.toString());
        return success("EstBertDetail retrieved successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping(value = "/{transactionPoid}/est-bert-details", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create EstBertDetail",
            description = "Create a new EstBertDetail for a port call operation",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> createEstBertDetail(@Parameter(description = "Transaction POID") @PathVariable Long transactionPoid,
                                                 @Valid @ModelAttribute PortCallOperationEstBertDetailRequestDto dto,
                                                 @RequestParam(value = "files", required = false) MultipartFile[] files,
                                                 @RequestParam(value = "remarks", required = false) String[] remarks,
                                                 @RequestParam(value = "checklistName", required = false) String[] checklistNames) {
        PortCallOperationEstBertDetailResponseDto result = portCallOperationService.createEstBertDetail(transactionPoid, dto, files, remarks, checklistNames);
        return success("EstBertDetail created successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping(value = "/{transactionPoid}/est-bert-details/{detRowId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Update EstBertDetail",
            description = "Update an existing EstBertDetail for a port call operation",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> updateEstBertDetail(@Parameter(description = "Transaction POID") @PathVariable Long transactionPoid,
                                                 @Parameter(description = "Detail Row ID") @PathVariable Long detRowId,
                                                 @Valid @ModelAttribute PortCallOperationEstBertDetailRequestDto dto,
                                                 @RequestParam(value = "files", required = false) MultipartFile[] files,
                                                 @RequestParam(value = "remarks", required = false) String[] remarks,
                                                 @RequestParam(value = "checklistName", required = false) String[] checklistNames) {
        PortCallOperationEstBertDetailResponseDto result = portCallOperationService.updateEstBertDetail(transactionPoid, detRowId, dto, files, remarks, checklistNames);
        return success("EstBertDetail updated successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/est-prearrival-details/activities")
    @Operation(
            summary = "List EstPrearrivalActDetails",
            description = "Retrieve list of activities for a specific prearrival detail",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> listEstPrearrivalActDetailsActivities(@Parameter(description = "Transaction POID") @PathVariable Long transactionPoid) {
        Map<String, Object> result = portCallOperationService.listEstPrearrivalActDetails(transactionPoid);
        loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), transactionPoid.toString());
        return success("EstPrearrivalActDetails retrieved successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping(value = "/{transactionPoid}/est-prearrival-details/activities", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create EstPrearrivalActDetail",
            description = "Create a new activity for a prearrival detail. detRowId will be generated automatically.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> createEstPrearrivalActDetail(@Parameter(description = "Transaction POID") @PathVariable Long transactionPoid,
                                                          @Valid @ModelAttribute PortCallOperationEstPrearrivalActDetailDto dto,
                                                          @RequestParam(value = "files", required = false) MultipartFile[] files,
                                                          @RequestParam(value = "remarks", required = false) String[] remarks,
                                                          @RequestParam(value = "checklistName", required = false) String[] checklistNames) {
        PortCallOperationEstPrearrivalActDetailResponseDto result = portCallOperationService.createEstPrearrivalActDetail(transactionPoid, dto, files, remarks, checklistNames);
        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, UserContext.getDocumentId(), transactionPoid.toString());
        return success("EstPrearrivalActDetail created successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping(value = "/{transactionPoid}/est-prearrival-details/{detRowId}/activities/{preActivityDtlPoid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Update EstPrearrivalActDetail",
            description = "Update an existing activity for a prearrival detail",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> updateEstPrearrivalActDetail(@Parameter(description = "Transaction POID") @PathVariable Long transactionPoid,
                                                          @Parameter(description = "Detail Row ID") @PathVariable Long detRowId,
                                                          @Parameter(description = "Pre Activity Detail POID") @PathVariable Long preActivityDtlPoid,
                                                          @Valid @ModelAttribute PortCallOperationEstPrearrivalActDetailDto dto,
                                                          @RequestParam(value = "files", required = false) MultipartFile[] files,
                                                          @RequestParam(value = "remarks", required = false) String[] remarks,
                                                          @RequestParam(value = "checklistName", required = false) String[] checklistNames) {
        PortCallOperationEstPrearrivalActDetailResponseDto result = portCallOperationService.updateEstPrearrivalActDetail(transactionPoid, detRowId, preActivityDtlPoid, dto, files, remarks, checklistNames);
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
        loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), transactionPoid.toString());
        return success("ActTimingsActvtyDetails retrieved successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping(value = "/{transactionPoid}/act-timing-details/activities", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create ActTimingsActvtyDetail",
            description = "Create a new activity for an actual timing detail. detRowId will be generated automatically.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> createActTimingsActvtyDetail(@Parameter(description = "Transaction POID") @PathVariable Long transactionPoid,
                                                          @Valid @ModelAttribute PortCallOperationActTimingsActivityDetailDto dto,
                                                          @RequestParam(value = "files", required = false) MultipartFile[] files,
                                                          @RequestParam(value = "remarks", required = false) String[] remarks,
                                                          @RequestParam(value = "checklistName", required = false) String[] checklistNames) {
        PortCallOperationActTimingsActvtyDetailResponseDto result = portCallOperationService.createActTimingsActvtyDetail(transactionPoid, dto, files, remarks, checklistNames);
        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, UserContext.getDocumentId(), transactionPoid.toString());
        return success("ActTimingsActvtyDetail created successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping(value = "/{transactionPoid}/act-timing-details/{detRowId}/activities/{actualsTimingDtlPoid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Update ActTimingsActvtyDetail",
            description = "Update an existing activity for an actual timing detail",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> updateActTimingsActvtyDetail(@Parameter(description = "Transaction POID") @PathVariable Long transactionPoid,
                                                          @Parameter(description = "Detail Row ID") @PathVariable Long detRowId,
                                                          @Parameter(description = "Actuals Timing Detail POID") @PathVariable Long actualsTimingDtlPoid,
                                                          @Valid @ModelAttribute PortCallOperationActTimingsActivityDetailDto dto,
                                                          @RequestParam(value = "files", required = false) MultipartFile[] files,
                                                          @RequestParam(value = "remarks", required = false) String[] remarks,
                                                          @RequestParam(value = "checklistName", required = false) String[] checklistNames) {
        PortCallOperationActTimingsActvtyDetailResponseDto result = portCallOperationService.updateActTimingsActvtyDetail(transactionPoid, detRowId, actualsTimingDtlPoid, dto, files, remarks, checklistNames);
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
    @PostMapping(value = "/{transactionPoid}/docs-copy-details", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create DocsCopyDetail",
            description = "Create a new DocsCopyDetail for a port call operation",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> createDocsCopyDetail(@Parameter(description = "Transaction POID") @PathVariable Long transactionPoid,
                                                  @Valid @ModelAttribute PortCallOperationDocsCopyDetailRequestDto dto,
                                                  @RequestParam(value = "files", required = false) MultipartFile[] files,
                                                  @RequestParam(value = "remarks", required = false) String[] remarks,
                                                  @RequestParam(value = "checklistName", required = false) String[] checklistNames) {
        PortCallOperationDocsCopyDetailResponseDto result = portCallOperationService.createDocsCopyDetail(transactionPoid, dto, files, remarks, checklistNames);
        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, UserContext.getDocumentId(), transactionPoid.toString());
        return success("DocsCopyDetail created successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping(value = "/{transactionPoid}/docs-copy-details/{detRowId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Update DocsCopyDetail",
            description = "Update an existing DocsCopyDetail for a port call operation",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> updateDocsCopyDetail(@Parameter(description = "Transaction POID") @PathVariable Long transactionPoid,
                                                  @Parameter(description = "Detail Row ID") @PathVariable Long detRowId,
                                                  @Valid @ModelAttribute PortCallOperationDocsCopyDetailRequestDto dto,
                                                  @RequestParam(value = "files", required = false) MultipartFile[] files,
                                                  @RequestParam(value = "remarks", required = false) String[] remarks,
                                                  @RequestParam(value = "checklistName", required = false) String[] checklistNames) {
        PortCallOperationDocsCopyDetailResponseDto result = portCallOperationService.updateDocsCopyDetail(transactionPoid, detRowId, dto, files, remarks, checklistNames);
        return success("DocsCopyDetail updated successfully", result);
    }

    // ------------------- PC Info Attachments (via common attachment service) -------------------

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping(value = "/{transactionPoid}/pc-info-attachments/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload PC Info attachments",
            description = "Upload multiple files as PC Info attachments using the common attachment service. Stored file names are appended to PC_INFO_ATTACHMENTS on the port call operation header (comma-separated).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> uploadPcInfoAttachments(@Parameter(description = "Transaction POID (port call operation id)") @PathVariable Long transactionPoid,
                                                     @RequestParam(value = "files", required = false) MultipartFile[] files,
                                                     @RequestParam(value = "remarks", required = false) String[] remarks,
                                                     @RequestParam(value = "checklistName", required = false) String[] checklistNames) {
        if (!pcInfoAttachmentService.isAttachmentServiceAvailable()) {
            return badRequest("Attachment service is not configured. Set common.service.attachment.base-url.");
        }
        if (files == null || files.length == 0) {
            return badRequest("No files provided for upload.");
        }
        PcInfoAttachmentUploadResponseDto response = pcInfoAttachmentService.uploadPcInfoAttachments(transactionPoid, files, remarks, checklistNames);
        String message = response.isHasErrors()
                ? "Files uploaded with some errors. Check 'errors' in response."
                : "PC Info attachments uploaded successfully.";
        return success(message, response);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/pc-info-attachments")
    @Operation(
            summary = "List PC Info attachments",
            description = "Retrieve paginated list of PC Info attachments for the port call operation from the common attachment service.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> listPcInfoAttachments(
            @Parameter(description = "Transaction POID") @PathVariable Long transactionPoid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (!pcInfoAttachmentService.isAttachmentServiceAvailable()) {
            return badRequest("Attachment service is not configured. Set common.service.attachment.base-url.");
        }
        Map<String, Object> result = pcInfoAttachmentService.listPcInfoAttachments(transactionPoid, page, size);
        return success("PC Info attachments fetched successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/pc-info-attachments/summary")
    @Operation(
            summary = "Get PC Info attachments summary",
            description = "Get comma-separated attachment names stored in PC_INFO_ATTACHMENTS for the port call operation.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> getPcInfoAttachmentsSummary(
            @Parameter(description = "Transaction POID") @PathVariable Long transactionPoid) {
        String summary = pcInfoAttachmentService.getPcInfoAttachmentsSummary(transactionPoid);
        return success("PC Info attachments summary", Map.of("pcInfoAttachments", summary != null ? summary : ""));
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/pc-info-attachments/{storedFileName}/download")
    @Operation(
            summary = "Download PC Info attachment",
            description = "Download a PC Info attachment file by its stored filename. Use the storedFileName from the list attachments response.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<org.springframework.core.io.Resource> downloadPcInfoAttachment(
            @Parameter(description = "Transaction POID") @PathVariable Long transactionPoid,
            @Parameter(description = "Stored filename (fileNameMapped) from attachment list response") @PathVariable String storedFileName) {
        if (!pcInfoAttachmentService.isAttachmentServiceAvailable()) {
            throw new IllegalStateException("Attachment service is not configured. Set common.service.attachment.base-url.");
        }
        return pcInfoAttachmentService.downloadAttachment(transactionPoid, storedFileName);
    }

    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{transactionPoid}/pc-info-attachments/{storedFileName}")
    @Operation(
            summary = "Delete PC Info attachment",
            description = "Delete a PC Info attachment file by its stored filename.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> deletePcInfoAttachment(
            @Parameter(description = "Transaction POID") @PathVariable Long transactionPoid,
            @Parameter(description = "Stored filename (fileNameMapped) from attachment list response") @PathVariable String storedFileName) {
        if (!pcInfoAttachmentService.isAttachmentServiceAvailable()) {
            return badRequest("Attachment service is not configured. Set common.service.attachment.base-url.");
        }
        pcInfoAttachmentService.deletePcInfoAttachment(transactionPoid, storedFileName);
        return success("Attachment deleted successfully", null);
    }

    // ------------------- Screen-specific attachments (isolated per screen) -------------------

    private ResponseEntity<?> requireAttachmentService() {
        if (!screenAttachmentService.isAttachmentServiceAvailable()) {
            return badRequest("Attachment service is not configured. Set common.service.attachment.base-url.");
        }
        return null;
    }

    // ----- Berthing (OPS_PC_EST_BERT_DTL.BERTHING_ATTACHMENTS) -----
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping(value = "/{transactionPoid}/berthing/{detRowId}/attachments/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload berthing attachments", description = "Upload attachments for berthing screen; stored in BERTHING_ATTACHMENTS.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> uploadBerthingAttachments(@PathVariable Long transactionPoid, @PathVariable Long detRowId,
                                                       @RequestParam(value = "files", required = false) MultipartFile[] files,
                                                       @RequestParam(value = "remarks", required = false) String[] remarks,
                                                       @RequestParam(value = "checklistName", required = false) String[] checklistNames) {
        ResponseEntity<?> err = requireAttachmentService();
        if (err != null) return err;
        if (files == null || files.length == 0) return badRequest("No files provided for upload.");
        PcInfoAttachmentUploadResponseDto response = screenAttachmentService.uploadBerthingAttachments(transactionPoid, detRowId, files, remarks, checklistNames);
        return success(response.isHasErrors() ? "Files uploaded with some errors." : "Berthing attachments uploaded successfully.", response);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/berthing/{detRowId}/attachments")
    @Operation(summary = "List berthing attachments", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> listBerthingAttachments(@PathVariable Long transactionPoid, @PathVariable Long detRowId,
                                                     @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        if (requireAttachmentService() != null) return requireAttachmentService();
        return success("Berthing attachments", screenAttachmentService.listBerthingAttachments(transactionPoid, detRowId, page, size));
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/berthing/{detRowId}/attachments/summary")
    @Operation(summary = "Berthing attachments summary", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> getBerthingAttachmentsSummary(@PathVariable Long transactionPoid, @PathVariable Long detRowId) {
        String summary = screenAttachmentService.getBerthingAttachmentsSummary(transactionPoid, detRowId);
        return success("Berthing attachments summary", Map.of("berthingAttachments", summary != null ? summary : ""));
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/berthing/{detRowId}/attachments/{storedFileName}/download")
    @Operation(summary = "Download berthing attachment", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<org.springframework.core.io.Resource> downloadBerthingAttachment(@PathVariable Long transactionPoid, @PathVariable Long detRowId, @PathVariable String storedFileName) {
        if (!screenAttachmentService.isAttachmentServiceAvailable())
            throw new IllegalStateException("Attachment service is not configured.");
        return screenAttachmentService.downloadBerthingAttachment(transactionPoid, detRowId, storedFileName);
    }

    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{transactionPoid}/berthing/{detRowId}/attachments/{storedFileName}")
    @Operation(summary = "Delete berthing attachment", description = "Deletes an attachment. Cannot delete the last attachment.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> deleteBerthingAttachment(@PathVariable Long transactionPoid, @PathVariable Long detRowId, @PathVariable String storedFileName) {
        if (requireAttachmentService() != null) return requireAttachmentService();
        screenAttachmentService.deleteBerthingAttachment(transactionPoid, detRowId, storedFileName);
        return success("Attachment deleted successfully", null);
    }

    // ----- Pre-arrival (OPS_PC_EST_PREARRIVAL_DTL.PRE_ARRIVAL_ATTACHMENTS) -----
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping(value = "/{transactionPoid}/pre-arrival/{detRowId}/attachments/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload pre-arrival attachments", description = "Stored in PRE_ARRIVAL_ATTACHMENTS.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> uploadPreArrivalAttachments(@PathVariable Long transactionPoid, @PathVariable Long detRowId,
                                                         @RequestParam(value = "files", required = false) MultipartFile[] files,
                                                         @RequestParam(value = "remarks", required = false) String[] remarks,
                                                         @RequestParam(value = "checklistName", required = false) String[] checklistNames) {
        if (requireAttachmentService() != null) return requireAttachmentService();
        if (files == null || files.length == 0) return badRequest("No files provided for upload.");
        PcInfoAttachmentUploadResponseDto response = screenAttachmentService.uploadPreArrivalAttachments(transactionPoid, detRowId, files, remarks, checklistNames);
        return success(response.isHasErrors() ? "Files uploaded with some errors." : "Pre-arrival attachments uploaded successfully.", response);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/pre-arrival/{detRowId}/attachments")
    @Operation(summary = "List pre-arrival attachments", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> listPreArrivalAttachments(@PathVariable Long transactionPoid, @PathVariable Long detRowId,
                                                       @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        if (requireAttachmentService() != null) return requireAttachmentService();
        return success("Pre-arrival attachments", screenAttachmentService.listPreArrivalAttachments(transactionPoid, detRowId, page, size));
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/pre-arrival/{detRowId}/attachments/summary")
    @Operation(summary = "Pre-arrival attachments summary", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> getPreArrivalAttachmentsSummary(@PathVariable Long transactionPoid, @PathVariable Long detRowId) {
        String summary = screenAttachmentService.getPreArrivalAttachmentsSummary(transactionPoid, detRowId);
        return success("Pre-arrival attachments summary", Map.of("preArrivalAttachments", summary != null ? summary : ""));
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/pre-arrival/{detRowId}/attachments/{storedFileName}/download")
    @Operation(summary = "Download pre-arrival attachment", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<org.springframework.core.io.Resource> downloadPreArrivalAttachment(@PathVariable Long transactionPoid, @PathVariable Long detRowId, @PathVariable String storedFileName) {
        if (!screenAttachmentService.isAttachmentServiceAvailable())
            throw new IllegalStateException("Attachment service is not configured.");
        return screenAttachmentService.downloadPreArrivalAttachment(transactionPoid, detRowId, storedFileName);
    }

    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{transactionPoid}/pre-arrival/{detRowId}/attachments/{storedFileName}")
    @Operation(summary = "Delete pre-arrival attachment", description = "Deletes an attachment. Cannot delete the last attachment.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> deletePreArrivalAttachment(@PathVariable Long transactionPoid, @PathVariable Long detRowId, @PathVariable String storedFileName) {
        if (requireAttachmentService() != null) return requireAttachmentService();
        screenAttachmentService.deletePreArrivalAttachment(transactionPoid, detRowId, storedFileName);
        return success("Attachment deleted successfully", null);
    }

    // ----- Other details / PDA-FDA (OPS_PC_OPERATION_HDR.PDA_FDA_ATTACHMENTS) -----
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping(value = "/{transactionPoid}/disbursement-other-details/attachments/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload other details (PDA/FDA) attachments", description = "Stored in PDA_FDA_ATTACHMENTS on header.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> uploadPdaFdaAttachments(@PathVariable Long transactionPoid,
                                                     @RequestParam(value = "files", required = false) MultipartFile[] files,
                                                     @RequestParam(value = "remarks", required = false) String[] remarks,
                                                     @RequestParam(value = "checklistName", required = false) String[] checklistNames) {
        if (requireAttachmentService() != null) return requireAttachmentService();
        if (files == null || files.length == 0) return badRequest("No files provided for upload.");
        PcInfoAttachmentUploadResponseDto response = screenAttachmentService.uploadPdaFdaAttachments(transactionPoid, files, remarks, checklistNames);
        return success(response.isHasErrors() ? "Files uploaded with some errors." : "Other details attachments uploaded successfully.", response);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/disbursement-other-details/attachments")
    @Operation(summary = "List other details attachments", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> listPdaFdaAttachments(@PathVariable Long transactionPoid, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        if (requireAttachmentService() != null) return requireAttachmentService();
        return success("Other details attachments", screenAttachmentService.listPdaFdaAttachments(transactionPoid, page, size));
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/disbursement-other-details/attachments/summary")
    @Operation(summary = "Other details attachments summary", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> getPdaFdaAttachmentsSummary(@PathVariable Long transactionPoid) {
        String summary = screenAttachmentService.getPdaFdaAttachmentsSummary(transactionPoid);
        return success("Other details attachments summary", Map.of("pdaFdaAttachments", summary != null ? summary : ""));
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/disbursement-other-details/attachments/{storedFileName}/download")
    @Operation(summary = "Download other details attachment", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<org.springframework.core.io.Resource> downloadPdaFdaAttachment(@PathVariable Long transactionPoid, @PathVariable String storedFileName) {
        if (!screenAttachmentService.isAttachmentServiceAvailable())
            throw new IllegalStateException("Attachment service is not configured.");
        return screenAttachmentService.downloadPdaFdaAttachment(transactionPoid, storedFileName);
    }

    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{transactionPoid}/disbursement-other-details/attachments/{storedFileName}")
    @Operation(summary = "Delete other details (PDA/FDA) attachment", description = "Deletes an attachment. Cannot delete the last attachment.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> deletePdaFdaAttachment(@PathVariable Long transactionPoid, @PathVariable String storedFileName) {
        if (requireAttachmentService() != null) return requireAttachmentService();
        screenAttachmentService.deletePdaFdaAttachment(transactionPoid, storedFileName);
        return success("Attachment deleted successfully", null);
    }

    // ----- Husbandry crew (OPS_PC_HUSBANDRY_CREW_DTL.CREW_ATTACHMENTS) -----
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping(value = "/{transactionPoid}/husbandry-crew/{detRowId}/attachments/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload husbandry crew attachments", description = "Stored in CREW_ATTACHMENTS.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> uploadHusbandryCrewAttachments(@PathVariable Long transactionPoid, @PathVariable Long detRowId,
                                                            @RequestParam(value = "files", required = false) MultipartFile[] files,
                                                            @RequestParam(value = "remarks", required = false) String[] remarks,
                                                            @RequestParam(value = "checklistName", required = false) String[] checklistNames) {
        if (requireAttachmentService() != null) return requireAttachmentService();
        if (files == null || files.length == 0) return badRequest("No files provided for upload.");
        PcInfoAttachmentUploadResponseDto response = screenAttachmentService.uploadHusbandryCrewAttachments(transactionPoid, detRowId, files, remarks, checklistNames);
        return success(response.isHasErrors() ? "Files uploaded with some errors." : "Husbandry crew attachments uploaded successfully.", response);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/husbandry-crew/{detRowId}/attachments")
    @Operation(summary = "List husbandry crew attachments", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> listHusbandryCrewAttachments(@PathVariable Long transactionPoid, @PathVariable Long detRowId,
                                                          @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        if (requireAttachmentService() != null) return requireAttachmentService();
        return success("Husbandry crew attachments", screenAttachmentService.listHusbandryCrewAttachments(transactionPoid, detRowId, page, size));
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/husbandry-crew/{detRowId}/attachments/summary")
    @Operation(summary = "Husbandry crew attachments summary", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> getHusbandryCrewAttachmentsSummary(@PathVariable Long transactionPoid, @PathVariable Long detRowId) {
        String summary = screenAttachmentService.getHusbandryCrewAttachmentsSummary(transactionPoid, detRowId);
        return success("Husbandry crew attachments summary", Map.of("crewAttachments", summary != null ? summary : ""));
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/husbandry-crew/{detRowId}/attachments/{storedFileName}/download")
    @Operation(summary = "Download husbandry crew attachment", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<org.springframework.core.io.Resource> downloadHusbandryCrewAttachment(@PathVariable Long transactionPoid, @PathVariable Long detRowId, @PathVariable String storedFileName) {
        if (!screenAttachmentService.isAttachmentServiceAvailable())
            throw new IllegalStateException("Attachment service is not configured.");
        return screenAttachmentService.downloadHusbandryCrewAttachment(transactionPoid, detRowId, storedFileName);
    }

    // ----- Husbandry other (OPS_PC_HUSBANDRY_OTH_DTL.ARRNGMNT_ATTACHMENTS) -----
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping(value = "/{transactionPoid}/husbandry-other/{detRowId}/attachments/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload husbandry other details attachments", description = "Stored in ARRNGMNT_ATTACHMENTS.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> uploadHusbandryOthAttachments(@PathVariable Long transactionPoid, @PathVariable Long detRowId,
                                                           @RequestParam(value = "files", required = false) MultipartFile[] files,
                                                           @RequestParam(value = "remarks", required = false) String[] remarks,
                                                           @RequestParam(value = "checklistName", required = false) String[] checklistNames) {
        if (requireAttachmentService() != null) return requireAttachmentService();
        if (files == null || files.length == 0) return badRequest("No files provided for upload.");
        PcInfoAttachmentUploadResponseDto response = screenAttachmentService.uploadHusbandryOthAttachments(transactionPoid, detRowId, files, remarks, checklistNames);
        return success(response.isHasErrors() ? "Files uploaded with some errors." : "Husbandry other attachments uploaded successfully.", response);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/husbandry-other/{detRowId}/attachments")
    @Operation(summary = "List husbandry other attachments", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> listHusbandryOthAttachments(@PathVariable Long transactionPoid, @PathVariable Long detRowId,
                                                         @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        if (requireAttachmentService() != null) return requireAttachmentService();
        return success("Husbandry other attachments", screenAttachmentService.listHusbandryOthAttachments(transactionPoid, detRowId, page, size));
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/husbandry-other/{detRowId}/attachments/summary")
    @Operation(summary = "Husbandry other attachments summary", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> getHusbandryOthAttachmentsSummary(@PathVariable Long transactionPoid, @PathVariable Long detRowId) {
        String summary = screenAttachmentService.getHusbandryOthAttachmentsSummary(transactionPoid, detRowId);
        return success("Husbandry other attachments summary", Map.of("arrngmntAttachments", summary != null ? summary : ""));
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/husbandry-other/{detRowId}/attachments/{storedFileName}/download")
    @Operation(summary = "Download husbandry other attachment", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<org.springframework.core.io.Resource> downloadHusbandryOthAttachment(@PathVariable Long transactionPoid, @PathVariable Long detRowId, @PathVariable String storedFileName) {
        if (!screenAttachmentService.isAttachmentServiceAvailable())
            throw new IllegalStateException("Attachment service is not configured.");
        return screenAttachmentService.downloadHusbandryOthAttachment(transactionPoid, detRowId, storedFileName);
    }

    // ----- Docs copy (OPS_PC_DOCS_COPY_DTL.DOCUMENT_ATTACHMENTS) -----
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping(value = "/{transactionPoid}/docs-copy/{detRowId}/attachments/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload docs copy attachments", description = "Stored in DOCUMENT_ATTACHMENTS.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> uploadDocsCopyAttachments(@PathVariable Long transactionPoid, @PathVariable Long detRowId,
                                                       @RequestParam(value = "files", required = false) MultipartFile[] files,
                                                       @RequestParam(value = "remarks", required = false) String[] remarks,
                                                       @RequestParam(value = "checklistName", required = false) String[] checklistNames) {
        if (requireAttachmentService() != null) return requireAttachmentService();
        if (files == null || files.length == 0) return badRequest("No files provided for upload.");
        PcInfoAttachmentUploadResponseDto response = screenAttachmentService.uploadDocsCopyAttachments(transactionPoid, detRowId, files, remarks, checklistNames);
        return success(response.isHasErrors() ? "Files uploaded with some errors." : "Docs copy attachments uploaded successfully.", response);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/docs-copy/{detRowId}/attachments")
    @Operation(summary = "List docs copy attachments", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> listDocsCopyAttachments(@PathVariable Long transactionPoid, @PathVariable Long detRowId,
                                                     @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        if (requireAttachmentService() != null) return requireAttachmentService();
        return success("Docs copy attachments", screenAttachmentService.listDocsCopyAttachments(transactionPoid, detRowId, page, size));
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/docs-copy/{detRowId}/attachments/summary")
    @Operation(summary = "Docs copy attachments summary", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> getDocsCopyAttachmentsSummary(@PathVariable Long transactionPoid, @PathVariable Long detRowId) {
        String summary = screenAttachmentService.getDocsCopyAttachmentsSummary(transactionPoid, detRowId);
        return success("Docs copy attachments summary", Map.of("documentAttachments", summary != null ? summary : ""));
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/docs-copy/{detRowId}/attachments/{storedFileName}/download")
    @Operation(summary = "Download docs copy attachment", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<org.springframework.core.io.Resource> downloadDocsCopyAttachment(@PathVariable Long transactionPoid, @PathVariable Long detRowId, @PathVariable String storedFileName) {
        if (!screenAttachmentService.isAttachmentServiceAvailable())
            throw new IllegalStateException("Attachment service is not configured.");
        return screenAttachmentService.downloadDocsCopyAttachment(transactionPoid, detRowId, storedFileName);
    }

    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{transactionPoid}/docs-copy/{detRowId}/attachments/{storedFileName}")
    @Operation(summary = "Delete docs copy attachment", description = "Deletes an attachment. Cannot delete the last attachment.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> deleteDocsCopyAttachment(@PathVariable Long transactionPoid, @PathVariable Long detRowId, @PathVariable String storedFileName) {
        if (requireAttachmentService() != null) return requireAttachmentService();
        screenAttachmentService.deleteDocsCopyAttachment(transactionPoid, detRowId, storedFileName);
        return success("Attachment deleted successfully", null);
    }

    // ----- Actual timing (OPS_PC_ACT_TIMING_DTL.TIMING_ATTACHMENTS) -----
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping(value = "/{transactionPoid}/actual-timing/{detRowId}/attachments/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload actual timing attachments", description = "Stored in TIMING_ATTACHMENTS.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> uploadTimingAttachments(@PathVariable Long transactionPoid, @PathVariable Long detRowId,
                                                     @RequestParam(value = "files", required = false) MultipartFile[] files,
                                                     @RequestParam(value = "remarks", required = false) String[] remarks,
                                                     @RequestParam(value = "checklistName", required = false) String[] checklistNames) {
        if (requireAttachmentService() != null) return requireAttachmentService();
        if (files == null || files.length == 0) return badRequest("No files provided for upload.");
        PcInfoAttachmentUploadResponseDto response = screenAttachmentService.uploadTimingAttachments(transactionPoid, detRowId, files, remarks, checklistNames);
        return success(response.isHasErrors() ? "Files uploaded with some errors." : "Actual timing attachments uploaded successfully.", response);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/actual-timing/{detRowId}/attachments")
    @Operation(summary = "List actual timing attachments", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> listTimingAttachments(@PathVariable Long transactionPoid, @PathVariable Long detRowId,
                                                   @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        if (requireAttachmentService() != null) return requireAttachmentService();
        return success("Actual timing attachments", screenAttachmentService.listTimingAttachments(transactionPoid, detRowId, page, size));
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/actual-timing/{detRowId}/attachments/summary")
    @Operation(summary = "Actual timing attachments summary", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> getTimingAttachmentsSummary(@PathVariable Long transactionPoid, @PathVariable Long detRowId) {
        String summary = screenAttachmentService.getTimingAttachmentsSummary(transactionPoid, detRowId);
        return success("Actual timing attachments summary", Map.of("timingAttachments", summary != null ? summary : ""));
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/actual-timing/{detRowId}/attachments/{storedFileName}/download")
    @Operation(summary = "Download actual timing attachment", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<org.springframework.core.io.Resource> downloadTimingAttachment(@PathVariable Long transactionPoid, @PathVariable Long detRowId, @PathVariable String storedFileName) {
        if (!screenAttachmentService.isAttachmentServiceAvailable())
            throw new IllegalStateException("Attachment service is not configured.");
        return screenAttachmentService.downloadTimingAttachment(transactionPoid, detRowId, storedFileName);
    }

    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{transactionPoid}/actual-timing/{detRowId}/attachments/{storedFileName}")
    @Operation(summary = "Delete actual timing attachment", description = "Deletes an attachment.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> deleteTimingAttachment(@PathVariable Long transactionPoid, @PathVariable Long detRowId, @PathVariable String storedFileName) {
        if (requireAttachmentService() != null) return requireAttachmentService();
        screenAttachmentService.deleteTimingAttachment(transactionPoid, detRowId, storedFileName);
        return success("Attachment deleted successfully", null);
    }

    @AllowedAction(UserRolesRightsEnum.PRINT)
    @Operation(summary = "Generate Excel for Husbandry Crew Details")
    @GetMapping("/excel/husbandryCrewDetails/{transactionPoid}")
    public ResponseEntity<byte[]> exportHusbandryCrewDetailsExcel(@PathVariable Long transactionPoid) {

        ExcelFileData data = excelExportService.generateExcel("110-163-crew", String.valueOf(transactionPoid), null, "Husbandry Crew Details.xlsx");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(data.getFileName()).build());

        return ResponseEntity.ok().headers(headers).body(data.getContent());
    }

    @AllowedAction(UserRolesRightsEnum.PRINT)
    @Operation(summary = "Generate Excel for PDA Charge Details")
    @GetMapping("/excel/pda/{transactionPoid}")
    public ResponseEntity<byte[]> exportPdaDetailsExcel(@PathVariable Long transactionPoid) {

        ExcelFileData data = excelExportService.generateExcel("110-163-pda", String.valueOf(transactionPoid), null, "PDA Charge Details.xlsx");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(data.getFileName()).build());

        return ResponseEntity.ok().headers(headers).body(data.getContent());
    }

    @GetMapping("/excel/fda/{transactionPoid}")
    public ResponseEntity<byte[]> exportFdaDetailsExcel(@PathVariable Long transactionPoid) {

        ExcelFileData data = excelExportService.generateExcel("110-163-fda", String.valueOf(transactionPoid), null, "FDA_Charge_Details.xlsx");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(data.getFileName()).build());

        return ResponseEntity.ok().headers(headers).body(data.getContent());
    }
}
