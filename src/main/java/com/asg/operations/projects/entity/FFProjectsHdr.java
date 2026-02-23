package com.asg.operations.projects.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "PROJECTS_HDR")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FFProjectsHdr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRANSACTION_POID", nullable = false)
    private Long transactionPoid;

    @Column(name = "TRANSACTION_DATE")
    private LocalDate transactionDate;

    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "DOC_REF", length = 50)
    private String docRef;

    @Column(name = "PROJECT_QUOTATION_POID")
    private Long quotationReferencePoid;

    @Column(name = "PROJECT_DESCRIPTION", length = 500)
    private String projectDescription;

    @Column(name = "BILLING_TO", length = 50)
    private String billingTo;

    @Column(name = "BILLING_PARTY_POID")
    private Long billingPartyPoid;

    @Column(name = "PROJECT_CUSTOMER_POID")
    private Long projectCustomerPoid;

    @Column(name = "PRINCIPAL_POID")
    private Long principalPoid;

    @Column(name = "SHIPMENT_MODE", length = 100)
    private String shipmentMode;

//    @Column(name = "MODE", length = 50)
//    private String mode;               // Not in DB

    @Column(name = "PROJECT_REFERENCE", length = 100)
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
    private Long carrierCodePoid;

    @Column(name = "COMMODITY", length = 500)
    private String commodity;

    @Column(name = "PROJECT_DETAILS", length = 1000)
    private String cargoDetails;

    @Column(name = "BILLING_CURRENCY_CODE", length = 10)
    private String billingCurrencyCode;

    @Column(name = "PROJECT_STATUS", length = 50)
    private String projectStatus;

//    @Column(name = "TOTAL_BUYING_RATE_BHD")
//    private Double totalBuyingRateBhd;

//    @Column(name = "TOTAL_VAT_BHD")
//    private Double totalVatBhd;

//    @Column(name = "GRAND_TOTAL_SELL_RATE_BHD")
//    private Double grandTotalSellRateBhd;
//
//    @Column(name = "GRAND_TOTAL_SELL_RATE_FC")
//    private Double grandTotalSellRateFc;

    @Column(name = "DELETED", length = 1)
    private String deleted = "N";

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;
}
