package com.asg.operations.salesquotationprojects.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class SalesQuoteProjectsResponse {

    private Long transactionPoid;
    private LocalDate transactionDate;
    private Long companyPoid;
    private String docRef;
    private String customerType;
    private BigDecimal customerPoid;
    private String customerName;
    private String customerContact;
    private String customerEmail;
    private String customerTelephone;
    private String customerMobile;
    private Long principalPoid;
    private String shipmentMode;
    private String transportationMode;
    private String otherMode;
    private Long linePoid;
    private Long carrierPoid;
    private String quoteReference;
    private String units;
    private BigDecimal weight;
    private BigDecimal cbm;
    private BigDecimal quantity;
    private BigDecimal freightTons;
    private BigDecimal autoRate;
    private String billingCurrencyCode;
    private BigDecimal agreedRate;
    private Long salesmanPoid;
    private String shippingTerms;
    private List<String> commodity;
    private String quotationStatus;
    private String projectDetails;
    private String isSupplementaryQuote;
    private String projectReferenceNumber;
    private LocalDate validityToDate;
    private Long termsPoid;
    private BigDecimal totalBuyingAmountLc;
    private BigDecimal totalTaxLc;
    private BigDecimal grantTotalSellAmountLc;
    private BigDecimal grantTotalSellAmountFc;
    private String remarks;
    private String actionStatus;
    private LocalDate actionDueDate;
    private Long bankAccountPoid;
    private String deleted;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;

    private List<SalesQuoteProjectsChargeDetailResponse> chargeDetails;
    private List<SalesQuoteProjectsNotesDetailResponse> notesDetails;
    private List<SalesQuoteProjectsTcDetailResponse> tcDetails;
}