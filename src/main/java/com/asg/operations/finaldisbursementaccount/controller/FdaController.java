package com.asg.operations.finaldisbursementaccount.controller;

import com.asg.common.lib.security.util.UserContext;
import com.asg.operations.common.ApiResponse;
import com.asg.operations.common.PageResponse;
import com.asg.operations.finaldisbursementaccount.dto.*;
import com.asg.operations.finaldisbursementaccount.service.FdaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.enums.UserRolesRightsEnum;
//import org.springframework.core.io.Resource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/fdas")
@RequiredArgsConstructor
@Tag(name = "FDA Management", description = "Final Disbursement Account operations")
public class FdaController {

    private final FdaService fdaService;

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping
    @Operation(summary = "Get FDA list", description = "Retrieve paginated list of Final Disbursement Accounts with optional filters")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "FDA list retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public ResponseEntity<?> getFdaList(
            @Parameter(description = "Page number (0-based)") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(required = false, defaultValue = "20") int size,
            @Parameter(description = "Sort field and direction (e.g., 'transactionPoid,desc')") @RequestParam(required = false) String sort,
            @Parameter(description = "Transaction identifier filter") @RequestParam(required = false) Long transactionPoid,
            @Parameter(description = "Vessel name filter") @RequestParam(required = false) String vesselName,
            @Parameter(description = "ETA from date filter (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate etaFrom,
            @Parameter(description = "ETA to date filter (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate etaTo
    ) {
        Sort sortObj = Sort.by(Sort.Direction.DESC, "transactionPoid");
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
        PageResponse<FdaHeaderDto> response = fdaService.getFdaList(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), transactionPoid, vesselName, etaFrom, etaTo, pageable);

        return ApiResponse.success("FDA list fetched successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}")
    @Operation(summary = "Get FDA by ID", description = "Retrieve a specific Final Disbursement Account by transaction ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "FDA retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "FDA not found")
    })
    public ResponseEntity<?> getFda(
            @Parameter(description = "Transaction identifier", required = true) @PathVariable Long transactionPoid
    ) {
        return ApiResponse.success("FDA fetched successfully", fdaService.getFdaHeader(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid()));
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping
    @Operation(summary = "Create FDA", description = "Create a new Final Disbursement Account")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "FDA created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid FDA data")
    })
    public ResponseEntity<?> createFda(
            @Parameter(description = "FDA header data", required = true) @Valid @RequestBody FdaHeaderDto dto
    ) {
        return ApiResponse.success("FDA created successfully", fdaService.createFdaHeader(dto, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId()));
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{transactionPoid}")
    @Operation(summary = "Update FDA", description = "Update an existing Final Disbursement Account")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "FDA updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "FDA not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid update data")
    })
    public ResponseEntity<?> updateFda(
            @Parameter(description = "Transaction identifier", required = true) @PathVariable Long transactionPoid,
            @Parameter(description = "FDA update data", required = true) @Valid @RequestBody UpdateFdaHeaderRequest dto
    ) {
        return ApiResponse.success("FDA updated successfully", fdaService.updateFdaHeader(transactionPoid, dto, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId()));
    }

    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{transactionPoid}")
    @Operation(summary = "Delete FDA", description = "Soft delete a Final Disbursement Account")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "FDA deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "FDA not found")
    })
    public ResponseEntity<?> deleteFda(
            @Parameter(description = "Transaction identifier", required = true) @PathVariable Long transactionPoid
    ) {
        fdaService.softDeleteFda(transactionPoid, UserContext.getUserId());
        return ApiResponse.success("FDA soft deleted successfully");
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/details")
    @Operation(summary = "Get FDA charges", description = "Retrieve paginated list of charges for a specific FDA")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Charges retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "FDA not found")
    })
    public ResponseEntity<?> getCharges(
            @Parameter(description = "Transaction identifier", required = true) @PathVariable Long transactionPoid,
            @Parameter(description = "Pagination parameters") Pageable pageable
    ) {
        PageResponse<FdaChargeDto> charges = fdaService.getCharges(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), pageable);
        return ApiResponse.success("Charges fetched successfully", charges);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("{transactionPoid}/details/bulk-save")
    @Operation(summary = "Bulk save charges", description = "Save multiple charges for a specific FDA")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Charges saved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid charge data")
    })
    public ResponseEntity<?> saveCharges(
            @Parameter(description = "Transaction identifier", required = true) @PathVariable Long transactionPoid,
            @Parameter(description = "List of charges to save", required = true) @RequestBody List<FdaChargeDto> charges
    ) {
        fdaService.saveCharges(transactionPoid, charges, UserContext.getUserId(), UserContext.getGroupPoid(), UserContext.getCompanyPoid());
        return ApiResponse.success("Charges saved successfully");
    }

    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{transactionPoid}/details/{detRowId}")
    @Operation(summary = "Delete FDA charge", description = "Delete a specific charge from an FDA")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Charge deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Charge not found")
    })
    public ResponseEntity<?> deleteFdaDetail(
            @Parameter(description = "Transaction identifier", required = true) @PathVariable Long transactionPoid,
            @Parameter(description = "Detail row identifier", required = true) @PathVariable Long detRowId
    ) {
        fdaService.deleteCharge(transactionPoid, detRowId, UserContext.getUserId());
        return ApiResponse.success("FDA detail deleted successfully");
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/close")
    @Operation(summary = "Close FDA", description = "Close a Final Disbursement Account")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "FDA closed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "FDA cannot be closed")
    })
    public ResponseEntity<?> closeFda(
            @Parameter(description = "Transaction identifier", required = true) @PathVariable Long transactionPoid
    ) {
        String result = fdaService.closeFda(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid(), transactionPoid);

        if (StringUtils.isNotBlank(result) && result.toUpperCase().contains("SUCCESS")) {
            return ApiResponse.success("FDA closed successfully");
        }
        if (StringUtils.isNotBlank(result) && result.toUpperCase().contains("WARNING")) {
            return ApiResponse.error(result, 400);
        } else {
            return ApiResponse.error(result, 500);
        }
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/reopen")
    @Operation(summary = "Reopen FDA", description = "Reopen a closed Final Disbursement Account")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "FDA reopened successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "FDA cannot be reopened")
    })
    public ResponseEntity<?> reopenFda(
            @Parameter(description = "Transaction identifier", required = true) @PathVariable Long transactionPoid,
            @Parameter(description = "Reopen request data", required = true) @RequestBody FdaReOpenDto fdaReOpenDto
    ) {
        String result = fdaService.reopenFda(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid(), transactionPoid, fdaReOpenDto);

        if (StringUtils.isNotBlank(result) && result.toUpperCase().contains("SUCCESS")) {
            return ApiResponse.success("FDA re-opened successfully");
        }
        if (StringUtils.isNotBlank(result) && result.toUpperCase().contains("WARNING")) {
            return ApiResponse.error(result, 400);
        } else {
            return ApiResponse.error(result, 500);
        }
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/submit")
    @Operation(summary = "Submit FDA", description = "Submit a Final Disbursement Account for approval")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "FDA submitted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "FDA cannot be submitted")
    })
    public ResponseEntity<?> submitFda(
            @Parameter(description = "Transaction identifier", required = true) @PathVariable Long transactionPoid
    ) {
        String result = fdaService.submitFda(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid(), transactionPoid);

        if (StringUtils.isNotBlank(result) && result.toUpperCase().contains("SUCCESS")) {
            return ApiResponse.success("FDA submitted for approval successfully");
        }
        if (StringUtils.isNotBlank(result) && result.toUpperCase().contains("WARNING")) {
            return ApiResponse.error(result, 400);
        } else {
            return ApiResponse.error(result, 500);
        }
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/verify")
    @Operation(summary = "Verify FDA", description = "Verify a submitted Final Disbursement Account")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "FDA verified successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "FDA cannot be verified")
    })
    public ResponseEntity<?> verifyFda(
            @Parameter(description = "Transaction identifier", required = true) @PathVariable Long transactionPoid
    ) {
        String result = fdaService.verifyFda(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid(), transactionPoid);

        if (StringUtils.isNotBlank(result) && result.toUpperCase().contains("SUCCESS")) {
            return ApiResponse.success("FDA verified successfully");
        }
        if (StringUtils.isNotBlank(result) && result.toUpperCase().contains("WARNING")) {
            return ApiResponse.error(result, 400);
        } else {
            return ApiResponse.error(result, 500);
        }
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/return")
    @Operation(summary = "Return FDA", description = "Return a Final Disbursement Account for corrections")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "FDA returned successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "FDA cannot be returned")
    })
    public ResponseEntity<?> returnFda(
            @Parameter(description = "Transaction identifier", required = true) @PathVariable Long transactionPoid,
            @Parameter(description = "Return request with correction remarks", required = true) @RequestBody @Valid FdaReturnDto request
    ) {
        String result = fdaService.returnFda(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid(), transactionPoid, request.getCorrectionRemarks());

        if (StringUtils.isNotBlank(result) && result.toUpperCase().contains("SUCCESS")) {
            return ApiResponse.success("FDA documents returned successfully");
        }
        if (StringUtils.isNotBlank(result) && result.toUpperCase().contains("WARNING")) {
            return ApiResponse.error(result, 400);
        } else {
            return ApiResponse.error(result, 500);
        }
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping("/{transactionPoid}/supplementary")
    @Operation(summary = "Create supplementary FDA", description = "Create a supplementary Final Disbursement Account")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Supplementary FDA created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Supplementary FDA cannot be created")
    })
    public ResponseEntity<?> supplementaryFda(
            @Parameter(description = "Transaction identifier", required = true) @PathVariable Long transactionPoid
    ) {
        String result = fdaService.supplementaryFda(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid(), transactionPoid);
        if (StringUtils.isNotBlank(result) && result.toUpperCase().contains("SUCCESS")) {
            return ApiResponse.success("Created FDA as supplementary successfully");
        }
        if (StringUtils.isNotBlank(result) && result.toUpperCase().contains("WARNING")) {
            return ApiResponse.error(result, 400);
        } else {
            return ApiResponse.error(result, 500);
        }
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/supplementary-info")
    @Operation(summary = "Get supplementary info", description = "Retrieve supplementary information for an FDA")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Supplementary info retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "FDA not found")
    })
    public ResponseEntity<?> getSupplementaryInfo(
            @Parameter(description = "Transaction identifier", required = true) @PathVariable Long transactionPoid
    ) {
        List<FdaSupplementaryInfoDto> dtos = fdaService.getSupplementaryInfo(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        return ApiResponse.success("Supplementary info fetched successfully", dtos);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/close-without-amount")
    @Operation(summary = "Close FDA without amount", description = "Close a Final Disbursement Account without specifying amounts")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "FDA closed without amount successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "FDA cannot be closed")
    })
    public ResponseEntity<?> closeWithoutAmount(
            @Parameter(description = "Transaction identifier", required = true) @PathVariable Long transactionPoid,
            @Parameter(description = "Closure remarks", required = true) @RequestParam String closedRemark
    ) {
        String result = fdaService.closeFdaWithoutAmount(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid(), closedRemark);
        if (StringUtils.isNotBlank(result) && result.toUpperCase().contains("SUCESS")) {
            return ApiResponse.success("Closed FDA without amount successfully");
        }
        if (StringUtils.isNotBlank(result) && result.toUpperCase().contains("WARNING")) {
            return ApiResponse.error(result, 400);
        } else {
            return ApiResponse.error(result, 500);
        }
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/party-gl")
    @Operation(summary = "Get party GL", description = "Retrieve General Ledger information for a specific party")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Party GL retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Party GL not found")
    })
    public ResponseEntity<?> getPartyGl(
            @Parameter(description = "Transaction identifier", required = true) @PathVariable Long transactionPoid,
            @Parameter(description = "Party identifier", required = true) @RequestParam Long partyPoid,
            @Parameter(description = "Party type", required = true) @RequestParam String partyType
    ) {
        PartyGlResponse response = fdaService.getPartyGl(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid(), partyPoid, partyType);
        return ApiResponse.success("Party General Ledger fetched successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping("/from-pda/{pdaTransactionPoid}")
    @Operation(summary = "Create FDA from PDA", description = "Create a Final Disbursement Account from a Preliminary Disbursement Account")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "FDA created from PDA successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "FDA cannot be created from PDA")
    })
    public ResponseEntity<?> createFromPda(
            @Parameter(description = "PDA transaction identifier", required = true) @PathVariable Long pdaTransactionPoid
    ) {
        String result = fdaService.createFdaFromPda(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid(), pdaTransactionPoid);
        if (StringUtils.isNotBlank(result) && result.toUpperCase().contains("SUCCESS")) {
            return ApiResponse.success("FDA created from PDA successfully");
        }
        if (StringUtils.isNotBlank(result) && result.toUpperCase().contains("WARNING")) {
            return ApiResponse.error(result, 400);
        } else {
            return ApiResponse.error(result, 500);
        }
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/logs/pda")
    @Operation(summary = "Get PDA logs", description = "Retrieve PDA-related logs for an FDA")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "PDA logs retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "FDA not found")
    })
    public ResponseEntity<?> getPdaLogs(
            @Parameter(description = "Transaction identifier", required = true) @PathVariable Long transactionPoid
    ) {

        List<PdaLogResponse> logs = fdaService.getPdaLogs(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());

        return ApiResponse.success("Logs fetched successfully", logs);
    }

//    @AllowedAction(UserRolesRightsEnum.PRINT)
//    @GetMapping(path = "/{transactionPoid}/print", produces = MediaType.APPLICATION_PDF_VALUE)
//    @Operation(summary = "Print FDA report", description = "Generate and download PDF report for an FDA")
//    @ApiResponses({
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "PDF report generated successfully",
//                    content = @Content(mediaType = "application/pdf")),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "FDA not found")
//    })
//    public ResponseEntity<Resource> printFda(
//            @Parameter(description = "Transaction identifier", required = true) @PathVariable("transactionPoid") Long transactionPoid,
//            @Parameter(description = "Report type", schema = @Schema(defaultValue = "default")) @RequestParam(value = "type", defaultValue = "default") String reportType) {
//
//        Resource pdfResource = fdaService.generateFdaReport(transactionPoid, reportType, UserContext.getCompanyPoid(), UserContext.getUserPoid(), UserContext.getGroupPoid());
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION,
//                        "attachment; filename=\"FDA_Report_" + transactionPoid + "_" + reportType + ".pdf\"")
//                .contentType(MediaType.APPLICATION_PDF)
//                .body(pdfResource);
//    }
}
