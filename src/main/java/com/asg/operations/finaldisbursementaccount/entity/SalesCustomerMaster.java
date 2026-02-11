package com.asg.operations.finaldisbursementaccount.entity;

import lombok.*;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "SALES_CUSTOMER_MASTER")
public class SalesCustomerMaster {

    @Id
    @Column(name = "CUSTOMER_POID", nullable = false)
    private Long customerPoid;

    @Column(name = "CUSTOMER_CODE", length = 20)
    private String customerCode;

    @Column(name = "CUSTOMER_NAME", length = 100)
    private String customerName;

    @Column(name = "CUSTOMER_NAME2", length = 100)
    private String customerName2;

    @Column(name = "CUSTOMER_CATEGORY_POID")
    private Long customerCategoryPoid;

    @Column(name = "ADDRESS_POID")
    private Long addressPoid;

    @Column(name = "CURRENCY_CODE", length = 20)
    private String currencyCode;

    @Column(name = "CREDIT_LIMIT")
    private BigDecimal creditLimit;

    @Column(name = "CREDIT_PERIOD")
    private Integer creditPeriod;

    @Column(name = "SALESMAN_POID")
    private Long salesmanPoid;

    @Column(name = "CR_REGNO", length = 50)
    private String crRegno;

    @Column(name = "BLOCKED_CUSTOMER", length = 1)
    private String blockedCustomer;

    @Column(name = "PAYMENT_TERMS", length = 100)
    private String paymentTerms;

    @Column(name = "DELIVERY_TERMS", length = 100)
    private String deliveryTerms;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @Column(name = "ACTIVE", length = 1)
    private String active;

    @Column(name = "CUSTOMER_TYPE", length = 20)
    private String customerType;

    @Column(name = "GL_POID", nullable = false)
    private Long glPoid;

    @Column(name = "CONTRACT_START")
    private LocalDate contractStart;

    @Column(name = "CONTRACT_END")
    private LocalDate contractEnd;

    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "TIN_NUMBER", length = 100)
    private String tinNumber;

    @Column(name = "REMARKS", length = 1000)
    private String remarks;
}
