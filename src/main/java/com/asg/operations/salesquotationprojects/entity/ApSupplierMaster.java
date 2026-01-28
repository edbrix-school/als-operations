package com.asg.operations.salesquotationprojects.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "AP_SUPPLIER_MASTER",
        uniqueConstraints = {
                @UniqueConstraint(name = "AP_SUPPLIER_MASTER_UK1", columnNames = "SUPPLIER_CODE"),
                @UniqueConstraint(name = "AP_SUPPLIER_MASTER_UK2", columnNames = "SUPPLIER_NAME")
        }
)
@Getter
@Setter
public class ApSupplierMaster {

    @Id
    @Column(name = "SUPPLIER_POID", nullable = false)
    private Long supplierPoid;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "SUPPLIER_CODE", nullable = false, length = 20)
    private String supplierCode;

    @Column(name = "SUPPLIER_NAME", nullable = false, length = 100)
    private String supplierName;

    @Column(name = "SUPPLIER_NAME2", length = 100)
    private String supplierName2;

    @Column(name = "SUPPLIER_TYPE", length = 100)
    private String supplierType;

    @Column(name = "SUPPLIER_CATEGORY_POID")
    private Long supplierCategoryPoid;

    @Column(name = "COUNTRY_POID")
    private Long countryPoid;

    @Column(name = "CREDIT_LIMIT")
    private BigDecimal creditLimit;

    @Column(name = "CREDIT_PERIOD")
    private Integer creditPeriod;

    @Column(name = "CR_NO", length = 50)
    private String crNo;

    @Column(name = "CONTACT_PERSON", length = 100)
    private String contactPerson;

    @Column(name = "ADDRESS_POID")
    private Long addressPoid;

    @Column(name = "ACTIVE", length = 1)
    private String active;

    @Column(name = "SEQNO")
    private Integer seqNo;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @Column(name = "GENERAL_REMARKS", length = 250)
    private String generalRemarks;

    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "TEMP_PAYMENT_NAME", length = 100)
    private String tempPaymentName;

    @Column(name = "CURRENCY_CODE", length = 20)
    private String currencyCode;

    @Column(name = "CURRENCY_RATE")
    private BigDecimal currencyRate;

    @Column(name = "RATE_EXPIRY_DATE")
    private LocalDate rateExpiryDate;

    @Column(name = "GL_POID")
    private Long glPoid;

    @Column(name = "DEFAULT_WEIGHT_SELECTION_MTD", length = 100)
    private String defaultWeightSelectionMtd;

    @Column(name = "PRODUCT_INFO", length = 1000)
    private String productInfo;

    @Column(name = "TIN_NUMBER", length = 100)
    private String tinNumber;

    @Column(name = "TAX_SLAB", length = 100)
    private String taxSlab;

    @Column(name = "EXEMPTION_REASON", length = 300)
    private String exemptionReason;

    @Column(name = "TAX_REGISTERED_DATE")
    private LocalDate taxRegisteredDate;

    @Column(name = "PURCHASER")
    private Long purchaser;

    @Column(name = "GRN_CREDIT_GL")
    private Long grnCreditGl;

    @Column(name = "AUDITED_YEAR")
    private LocalDate auditedYear;

    @Column(name = "AUDITING_FIRM", length = 1000)
    private String auditingFirm;

    @Column(name = "ISO_CERTIFICATION", length = 1000)
    private String isoCertification;

    @Column(name = "PROFILE_UPDATED", length = 100)
    private String profileUpdated;

    @Column(name = "PROFILE_VAT_CR_MISMATCH", length = 100)
    private String profileVatCrMismatch;

    @Column(name = "CUSTOMER_POID")
    private Long customerPoid;
}
