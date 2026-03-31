package com.asg.operations.salesquotationprojects.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.operations.salesquotationprojects.dto.AddressDetailsDto;
import com.asg.operations.salesquotationprojects.dto.SalesQuoteProjectsRequest;
import com.asg.operations.salesquotationprojects.dto.SalesQuoteProjectsResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public interface SalesQuoteProjectsService {

    Map<String, Object> listSalesQuoteProjectsWithFilters(String documentId, FilterRequestDto filterRequest, Pageable pageable, LocalDate periodFrom, LocalDate periodTo);

    SalesQuoteProjectsResponse getSalesQuoteProjectById(Long transactionPoid);

    SalesQuoteProjectsResponse createSalesQuoteProject(SalesQuoteProjectsRequest request);

    SalesQuoteProjectsResponse updateSalesQuoteProject(Long transactionPoid, SalesQuoteProjectsRequest request);

    void deleteSalesQuoteProject(Long transactionPoid, DeleteReasonDto deleteReasonDto);

    // Stored Procedure Methods
    Map<String, Object> getCustomerAddress(Long customerPoid);

    Map<String, Object> getTermsAndConditions(Long templatePoid, Long docKeyPoid);

    Map<String, Object> getChargeTaxDetails(LocalDateTime transactionDate, Long companyPoid, Long partyPoid, Long chargePoid);

    AddressDetailsDto getCustomerDetailsById(BigDecimal addressPoid);
}