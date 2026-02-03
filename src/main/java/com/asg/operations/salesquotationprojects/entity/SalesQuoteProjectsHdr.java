package com.asg.operations.salesquotationprojects.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "SALES_QUOTE_PROJECTS_HDR",
        uniqueConstraints = {
                @UniqueConstraint(name = "SALES_QUOTE_PROJ_HDR_UK1", columnNames = "DOC_REF")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesQuoteProjectsHdr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRANSACTION_POID", nullable = false)
    private Long transactionPoid;

    @Column(name = "TRANSACTION_DATE")
    private LocalDate transactionDate;

    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "DOC_REF", nullable = false, length = 25)
    private String docRef;

    @Column(name = "CUSTOMER_TYPE", length = 50)
    private String customerType;

    @Column(name = "CUSTOMER_POID")
    private BigDecimal customerPoid;

    @Column(name = "CUSTOMER_NAME", length = 300)
    private String customerName;

    @Column(name = "CUSTOMER_CONTACT", length = 300)
    private String customerContact;

    @Column(name = "CUSTOMER_EMAIL", length = 300)
    private String customerEmail;

    @Column(name = "CUSTOMER_TELEPHONE", length = 300)
    private String customerTelephone;

    @Column(name = "CUSTOMER_MOBILE", length = 100)
    private String customerMobile;

    @Column(name = "PRINCIPAL_POID")
    private Long principalPoid;

    @Column(name = "SHIPMENT_MODE", length = 100)
    private String shipmentMode;

    @Column(name = "TRANSPORTATION_MODE", length = 100)
    private String transportationMode;

    @Column(name = "OTHER_MODE", length = 100)
    private String otherMode;

    @Column(name = "LINE_POID")
    private Long linePoid;

    @Column(name = "CARRIER_POID")
    private Long carrierPoid;

    @Column(name = "QUOTE_REFERENCE", length = 100)
    private String quoteReference;

    @Column(name = "UNITS", length = 50)
    private String units;

    @Column(name = "WEIGHT")
    private BigDecimal weight;

    @Column(name = "CBM")
    private BigDecimal cbm;

    @Column(name = "QUANTITY")
    private BigDecimal quantity;

    @Column(name = "FREIGHT_TONS")
    private BigDecimal freightTons;

    @Column(name = "AUTO_RATE")
    private BigDecimal autoRate;

    @Column(name = "BILLING_CURRENCY_CODE", length = 10)
    private String billingCurrencyCode;

    @Column(name = "AGREED_RATE")
    private BigDecimal agreedRate;

    @Column(name = "SALESMAN_POID")
    private Long salesmanPoid;

    @Column(name = "SHIPPING_TERMS", length = 20)
    private String shippingTerms;

    @Column(name = "COMMODITY", length = 1000)
    private String commodity;

    @Column(name = "QUOTATION_STATUS", length = 20)
    private String quotationStatus;

    @Column(name = "PROJECT_DETAILS", length = 1000)
    private String projectDetails;

    @Column(name = "IS_SUPPLEMENTARY_QUOTE", length = 1)
    private String isSupplementaryQuote;

    @Column(name = "PROJECT_REFERENCE_NUMBER", length = 100)
    private String projectReferenceNumber;

    @Column(name = "VALIDITY_TO_DATE")
    private LocalDate validityToDate;

    @Column(name = "TERMS_POID")
    private Long termsPoid;

    @Column(name = "TOTAL_BUYING_AMOUNT_LC")
    private BigDecimal totalBuyingAmountLc;

    @Column(name = "TOTAL_TAX_LC")
    private BigDecimal totalTaxLc;

    @Column(name = "GRANT_TOTAL_SELL_AMOUNT_LC")
    private BigDecimal grantTotalSellAmountLc;

    @Column(name = "GRANT_TOTAL_SELL_AMOUNT_FC")
    private BigDecimal grantTotalSellAmountFc;

    @Column(name = "REMARKS", length = 500)
    private String remarks;

    @Column(name = "ACTION_STATUS", length = 20)
    private String actionStatus;

    @Column(name = "ACTION_DUE_DATE")
    private LocalDate actionDueDate;

    @Column(name = "BANK_ACCOUNT_POID")
    private Long bankAccountPoid;

    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;
}
