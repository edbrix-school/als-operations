package com.asg.operations.salesquotationprojects.dto;

import com.asg.operations.salesquotationprojects.annotation.ValidTransportationMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@ValidTransportationMode
public class SalesQuoteProjectsRequest {

    @NotBlank(message = "Customer type is required")
    @Size(max = 50, message = "Customer type cannot exceed 50 characters")
    private String customerType;

    private BigDecimal customerPoid;

    @NotBlank(message = "Customer name is required")
    @Size(max = 300, message = "Customer name cannot exceed 300 characters")
    private String customerName;
    
    @Size(max = 300, message = "Customer contact cannot exceed 300 characters")
    private String customerContact;
    
    @Size(max = 300, message = "Customer email cannot exceed 300 characters")
    private String customerEmail;
    
    @Size(max = 300, message = "Customer telephone cannot exceed 300 characters")
    private String customerTelephone;
    
    @Size(max = 100, message = "Customer mobile cannot exceed 100 characters")
    private String customerMobile;

    @NotNull(message = "Principal Poid is required")
    private Long principalPoid;

    @NotBlank(message = "Shipment mode is required")
    @Size(max = 100, message = "Shipment mode cannot exceed 100 characters")
    private String shipmentMode;

    @NotBlank(message = "Transportation mode is required")
    @Size(max = 100, message = "Transportation mode cannot exceed 100 characters")
    private String transportationMode;

    @Size(max = 100, message = "Other mode cannot exceed 100 characters")
    private String otherMode;
    
    private Long linePoid;
    private Long carrierPoid;
    
    @Size(max = 100, message = "Quote reference cannot exceed 100 characters")
    private String quoteReference;
    
    @Size(max = 50, message = "Units cannot exceed 50 characters")
    private String units;
    
    private BigDecimal weight;
    private BigDecimal cbm;
    private BigDecimal quantity;
    private BigDecimal freightTons;
    private BigDecimal autoRate;
    
    @Size(max = 10, message = "Billing currency code cannot exceed 10 characters")
    private String billingCurrencyCode;
    
    private BigDecimal agreedRate;

    @NotNull(message = "Salesman POID is required")
    private Long salesmanPoid;
    
    @Size(max = 20, message = "Shipping terms cannot exceed 20 characters")
    private String shippingTerms;

    @NotEmpty(message = "Commodity is required")
    @Size(max = 1000, message = "Commodity cannot exceed 1000 characters")
    private List<String> commodity;
    
    @Size(max = 20, message = "Quotation status cannot exceed 20 characters")
    private String quotationStatus;
    
    @Size(max = 1000, message = "Project details cannot exceed 1000 characters")
    private String projectDetails;
    
    @Size(max = 1, message = "Is supplementary quote must be Y or N")
    private String isSupplementaryQuote;
    
    @Size(max = 100, message = "Project reference number cannot exceed 100 characters")
    private String projectReferenceNumber;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validityToDate;

    private LocalDate transactionDate;
    
    private Long termsPoid;
    private BigDecimal totalBuyingAmountLc;
    private BigDecimal totalTaxLc;
    private BigDecimal grantTotalSellAmountLc;
    private BigDecimal grantTotalSellAmountFc;
    
    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;
    
    @Size(max = 20, message = "Action status cannot exceed 20 characters")
    private String actionStatus;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actionDueDate;
    
    private Long bankAccountPoid;
    
    @Valid
    private List<SalesQuoteProjectsChargeDetailRequest> chargeDetails;
    
    @Valid
    private List<SalesQuoteProjectsNotesDetailRequest> notesDetails;
    
    @Valid
    private List<SalesQuoteProjectsTcDetailRequest> tcDetails;
}