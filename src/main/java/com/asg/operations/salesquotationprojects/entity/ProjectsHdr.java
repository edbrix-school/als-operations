package com.asg.operations.salesquotationprojects.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "PROJECTS_HDR",
        uniqueConstraints = {
                @UniqueConstraint(name = "PROJECTS_HDR_UK1", columnNames = {"DOC_REF"})
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectsHdr {

    @AuditIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "projects_hdr_seq")
    @SequenceGenerator(name = "projects_hdr_seq", sequenceName = "PROJECTS_HDR_SEQ", allocationSize = 1)
    @Column(name = "TRANSACTION_POID", nullable = false)
    private Long transactionPoid;

    @AuditIgnore
    @Column(name = "TRANSACTION_DATE")
    private LocalDate transactionDate;

    @AuditIgnore
    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "DOC_REF", length = 25, nullable = false)
    private String docRef;

    @Column(name = "PROJECT_QUOTATION_POID")
    private Long projectQuotationPoid;

    @Column(name = "PROJECT_DESCRIPTION", length = 500)
    private String projectDescription;

    @Column(name = "BILLING_TO", length = 100)
    private String billingTo;

    @Column(name = "BILLING_PARTY_POID")
    private Long billingPartyPoid;

    @Column(name = "PROJECT_CUSTOMER_POID")
    private Long projectCustomerPoid;

    @Column(name = "PRINCIPAL_POID")
    private Long principalPoid;

    @Column(name = "SHIPMENT_MODE", length = 100)
    private String shipmentMode;

    @Column(name = "TRANSPORTATION_MODE", length = 100)
    private String transportationMode;

    @Column(name = "PROJECT_REFERENCE", length = 300)
    private String projectReference;

    @Column(name = "PERIOD_FROM")
    private LocalDate periodFrom;

    @Column(name = "PERIOD_TO")
    private LocalDate periodTo;

    @Column(name = "SALESMAN_POID")
    private Long salesmanPoid;

    @Column(name = "LINE_POID")
    private Long linePoid;

    @Column(name = "CARRIER_POID")
    private Long carrierPoid;

    @Column(name = "COMMODITY", length = 1000)
    private String commodity;

    @Column(name = "PROJECT_DETAILS", length = 1000)
    private String projectDetails;

    @Column(name = "UNITS", length = 50)
    private String units;

    @Column(name = "WEIGHT")
    private Double weight;

    @Column(name = "CBM")
    private Double cbm;

    @Column(name = "QUANTITY")
    private Double quantity;

    @Column(name = "FREIGHT_TONS")
    private Double freightTons;

    @Column(name = "AUTO_RATE")
    private Double autoRate;

    @Column(name = "BILLING_CURRENCY_CODE", length = 10)
    private String billingCurrencyCode;

    @Column(name = "AGREED_RATE")
    private Double agreedRate;

    @Column(name = "PROJECT_STATUS", length = 20)
    private String projectStatus;

    @Column(name = "TOTAL_BUYING_AMOUNT_LC")
    private Double totalBuyingAmountLc;

    @Column(name = "TOTAL_TAX")
    private Double totalTax;

    @Column(name = "GRANT_TOTAL_SELL_AMOUNT_LC")
    private Double grantTotalSellAmountLc;

    @Column(name = "GRANT_TOTAL_SELL_AMOUNT_FC")
    private Double grantTotalSellAmountFc;

    @AuditIgnore
    @Column(name = "DELETED", length = 1)
    private String deleted = "N";

    @AuditIgnore
    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @AuditIgnore
    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @AuditIgnore
    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @AuditIgnore
    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;
}
