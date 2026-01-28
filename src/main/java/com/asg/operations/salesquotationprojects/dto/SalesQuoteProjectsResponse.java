package com.asg.operations.salesquotationprojects.dto;

import com.asg.common.lib.dto.LovGetListDto;
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
    private LovGetListDto companyDet;
    private String docRef;
    private String customerType;
    private Long customerPoid;
    private LovGetListDto customerDet;
    private String customerName;
    private String customerContact;
    private String customerEmail;
    private String customerTelephone;
    private String customerMobile;
    private Long principalPoid;
    private LovGetListDto principalDet;
    private String shipmentMode;
    private String transportationMode;
    private String otherMode;
    private Long linePoid;
    private LovGetListDto lineDet;
    private Long carrierPoid;
    private LovGetListDto carrierDet;
    private String quoteReference;
    private String units;
    private BigDecimal weight;
    private BigDecimal cbm;
    private BigDecimal quantity;
    private BigDecimal freightTons;
    private BigDecimal autoRate;
    private String billingCurrencyCode;
    private LovGetListDto billingCurrencyDet;
    private BigDecimal agreedRate;
    private Long salesmanPoid;
    private LovGetListDto salesmanDet;
    private String shippingTerms;
    private List<String> commodity;
    private List<LovGetListDto> commodityDet;
    private String quotationStatus;
    private String projectDetails;
    private String isSupplementaryQuote;
    private String projectReferenceNumber;
    private LovGetListDto projectDet;
    private LocalDate validityToDate;
    private Long termsPoid;
    private LovGetListDto termsDet;
    private BigDecimal totalBuyingAmountLc;
    private BigDecimal totalTaxLc;
    private BigDecimal grantTotalSellAmountLc;
    private BigDecimal grantTotalSellAmountFc;
    private String remarks;
    private String actionStatus;
    private LocalDate actionDueDate;
    private Long bankAccountPoid;
    private LovGetListDto bankAccountDet;
    private String deleted;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;

    private List<SalesQuoteProjectsChargeDetailResponse> chargeDetails;
    private List<SalesQuoteProjectsNotesDetailResponse> notesDetails;
    private List<SalesQuoteProjectsTcDetailResponse> tcDetails;
}