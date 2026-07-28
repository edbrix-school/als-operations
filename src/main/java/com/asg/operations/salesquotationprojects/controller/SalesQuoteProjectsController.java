package com.asg.operations.salesquotationprojects.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.excel.ExcelFileData;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDownloadHeaderService;
import com.asg.common.lib.service.ExcelExportService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.operations.common.ApiResponse;
import com.asg.operations.salesquotationprojects.dto.AddressDetailsDto;
import com.asg.operations.salesquotationprojects.dto.SalesQuoteProjectsRequest;
import com.asg.operations.salesquotationprojects.dto.SalesQuoteProjectsResponse;
import com.asg.operations.salesquotationprojects.entity.SalesQuoteProjectsHdr;
import com.asg.operations.salesquotationprojects.service.SalesQuoteProjectsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.error;
import static com.asg.common.lib.dto.response.ApiResponse.success;

@Slf4j
@RestController
@RequestMapping("/v1/sales-quotation-projects")
@Tag(name = "Sales Quotation Projects", description = "APIs for managing Sales Quotation Projects records")
@RequiredArgsConstructor
public class SalesQuoteProjectsController {

    private final SalesQuoteProjectsService salesQuoteProjectsService;
    private final LoggingService loggingService;
    private final ExcelExportService excelExportService;
    private final DocumentDownloadHeaderService downloadHeaderService;

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/list")
    public ResponseEntity<?> getSalesQuoteProjectsList(@RequestBody(required = false) FilterRequestDto filterRequest,
                                                       @ParameterObject Pageable pageable,
                                                       @RequestParam(required = false) LocalDate periodFrom,
                                                       @RequestParam(required = false) LocalDate periodTo) {
        Map<String, Object> salesQuoteProjectsPage = salesQuoteProjectsService.listSalesQuoteProjectsWithFilters(UserContext.getDocumentId(), filterRequest, pageable, periodFrom, periodTo);
        return success("Sales Quotation Projects fetched successfully", salesQuoteProjectsPage);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}")
    public ResponseEntity<?> getSalesQuoteProjectById(@PathVariable @NotNull Long transactionPoid) {
        SalesQuoteProjectsResponse response = salesQuoteProjectsService.getSalesQuoteProjectById(transactionPoid);
        loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), transactionPoid.toString());
        return ApiResponse.success("Sales Quotation Project retrieved successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping
    public ResponseEntity<?> createSalesQuoteProject(@Valid @RequestBody SalesQuoteProjectsRequest request) {
        SalesQuoteProjectsResponse response = salesQuoteProjectsService.createSalesQuoteProject(request);
        return ApiResponse.success("Sales Quotation Project created successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{transactionPoid}")
    public ResponseEntity<?> updateSalesQuoteProject(@PathVariable @NotNull Long transactionPoid,
                                                     @Valid @RequestBody SalesQuoteProjectsRequest request) {
        SalesQuoteProjectsResponse response = salesQuoteProjectsService.updateSalesQuoteProject(transactionPoid, request);
        return ApiResponse.success("Sales Quotation Project updated successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{transactionPoid}")
    public ResponseEntity<?> deleteSalesQuoteProject(@PathVariable @NotNull Long transactionPoid,
                                                     @Valid @RequestBody(required = false) DeleteReasonDto deleteReasonDto) {
        salesQuoteProjectsService.deleteSalesQuoteProject(transactionPoid, deleteReasonDto);
        return ApiResponse.success("Sales Quotation Project deleted successfully");
    }

    // Stored Procedure Endpoints
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/customer-address/{customerPoid}")
    public ResponseEntity<?> getCustomerAddress(@PathVariable Long customerPoid) {
        Map<String, Object> result = salesQuoteProjectsService.getCustomerAddress(customerPoid);
        return success("Customer address retrieved successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/terms-conditions/{termsPoid}/{docKeyPoid}")
    public ResponseEntity<?> getTermsAndConditions(@PathVariable Long termsPoid, @PathVariable Long docKeyPoid) {
        Map<String, Object> result = salesQuoteProjectsService.getTermsAndConditions(termsPoid, docKeyPoid);
        return success("Terms and conditions retrieved successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/charge-tax-details/{transactionDate}/{companyPoid}/{partyPoid}/{chargePoid}")
    public ResponseEntity<?> getChargeTaxDetails(@PathVariable LocalDateTime transactionDate, @PathVariable Long companyPoid, @PathVariable(required = false) Long partyPoid, @PathVariable Long chargePoid) {
        Map<String, Object> result = salesQuoteProjectsService.getChargeTaxDetails(transactionDate, companyPoid, partyPoid, chargePoid);
        return success("Charge tax details retrieved successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/customer-details/{addressPoid}")
    public ResponseEntity<?> getCustomerDetailsById(@PathVariable @NotNull BigDecimal addressPoid) {
        if (addressPoid == null) {
            return ApiResponse.badRequest("Address POID is required");
        }
        log.info("Fetching customer details for addressPoid: {}", addressPoid);

        AddressDetailsDto response = salesQuoteProjectsService.getCustomerDetailsById(addressPoid);

        loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), addressPoid.toString());
        return ApiResponse.success("Customer details retrieved successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.PRINT)
    @Operation(summary = "Generate Excel for Sales Quotation Projects Charge Details")
    @GetMapping("/excel/{transactionPoid}")
    public ResponseEntity<?> exportSalesQuotationProjectsChargeDetailsExcel(@PathVariable Long transactionPoid) {

        ExcelFileData data = excelExportService.generateExcel("140-100", String.valueOf(transactionPoid), null, "Sales Quotation Projects Charge Details.xlsx");

        HttpHeaders headers = downloadHeaderService.buildAttachmentHeaders(
                SalesQuoteProjectsHdr.class, transactionPoid, "sales-quotation-projects-charge-details", "xlsx");
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        return ResponseEntity.ok().headers(headers).body(data.getContent());
    }

    @AllowedAction(UserRolesRightsEnum.PRINT)
    @GetMapping("/print/{transactionPoid}")
    public ResponseEntity<?> print(@PathVariable Long transactionPoid) {
        try {
            byte[] pdf = salesQuoteProjectsService.print(transactionPoid);
            return ResponseEntity.ok()
                    .headers(downloadHeaderService.buildAttachmentHeaders(
                            SalesQuoteProjectsHdr.class,
                            transactionPoid,
                            "sales-quotation-project",
                            "pdf"))
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return error("Failed to generate PDF: " + e.getMessage(), 500);
        }
    }
}
