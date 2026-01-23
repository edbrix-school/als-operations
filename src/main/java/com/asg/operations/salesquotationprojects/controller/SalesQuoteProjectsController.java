package com.asg.operations.salesquotationprojects.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.operations.common.ApiResponse;
import com.asg.operations.salesquotationprojects.dto.SalesQuoteProjectsRequest;
import com.asg.operations.salesquotationprojects.dto.SalesQuoteProjectsResponse;
import com.asg.operations.salesquotationprojects.service.SalesQuoteProjectsService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.internalServerError;
import static com.asg.common.lib.dto.response.ApiResponse.success;

@RestController
@RequestMapping("/v1/sales-quote-projects")
@Tag(name = "Sales Quotation Projects", description = "APIs for managing Sales Quotation Projects records")
@RequiredArgsConstructor
public class SalesQuoteProjectsController {

    private final SalesQuoteProjectsService salesQuoteProjectsService;
    private final LoggingService loggingService;

    @Operation(summary = "Get all Sales Quote Projects", description = "Returns paginated list of Sales Quote Projects with optional filters. Supports pagination with page and size parameters.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Sales Quote Projects list fetched successfully", content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/list")
    public ResponseEntity<?> getSalesQuoteProjectsList(@RequestBody(required = false) FilterRequestDto filterRequest,
                                                       @ParameterObject Pageable pageable,
                                                       @RequestParam(required = false) LocalDate periodFrom,
                                                       @RequestParam(required = false) LocalDate periodTo) {
        Map<String, Object> salesQuoteProjectsPage = salesQuoteProjectsService.listSalesQuoteProjectsWithFilters(UserContext.getDocumentId(), filterRequest, pageable, periodFrom, periodTo);
        return success("Sales Quote Projects list fetched successfully", salesQuoteProjectsPage);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}")
    public ResponseEntity<?> getSalesQuoteProjectById(@PathVariable @NotNull Long transactionPoid
    ) {
        SalesQuoteProjectsResponse response = salesQuoteProjectsService.getSalesQuoteProjectById(transactionPoid);
        loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), transactionPoid.toString());
        return ApiResponse.success("Sales Quote Project retrieved successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping
    public ResponseEntity<?> createSalesQuoteProject(@Valid @RequestBody SalesQuoteProjectsRequest request
    ) {
        SalesQuoteProjectsResponse response = salesQuoteProjectsService.createSalesQuoteProject(request);
        return ApiResponse.success("Sales Quote Project created successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{transactionPoid}")
    public ResponseEntity<?> updateSalesQuoteProject(@PathVariable @NotNull Long transactionPoid,
                                                     @Valid @RequestBody SalesQuoteProjectsRequest request
    ) {
        SalesQuoteProjectsResponse response = salesQuoteProjectsService.updateSalesQuoteProject(transactionPoid, request);
        return ApiResponse.success("Sales Quote Project updated successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{transactionPoid}")
    public ResponseEntity<?> deleteSalesQuoteProject(@PathVariable @NotNull Long transactionPoid,
                                                     @Valid @RequestBody(required = false) DeleteReasonDto deleteReasonDto
    ) {
        salesQuoteProjectsService.deleteSalesQuoteProject(transactionPoid, deleteReasonDto);
        return ApiResponse.success("Sales Quote Project deleted successfully");
    }
}