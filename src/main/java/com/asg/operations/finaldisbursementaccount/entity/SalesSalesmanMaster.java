package com.asg.operations.finaldisbursementaccount.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "SALES_SALESMAN_MASTER")
@Getter
@Setter
public class SalesSalesmanMaster {

    @Id
    @Column(name = "SALESMAN_POID", nullable = false)
    private Long salesmanPoid;

    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "SALESMAN_CODE", length = 20)
    private String salesmanCode;

    @Column(name = "SALESMAN_NAME", length = 100)
    private String salesmanName;

    @Column(name = "SALESMAN_NAME2", length = 100)
    private String salesmanName2;

    @Column(name = "SALESMAN_GL_CODE", length = 20)
    private String salesmanGlCode;

    @Column(name = "ACTIVE", length = 1)
    private String active = "Y";

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

    @Column(name = "USER_POID")
    private Long userPoid;

    @Column(name = "REPORTING_TO", length = 30)
    private String reportingTo;

    @Column(name = "MOBILE", length = 20)
    private String mobile;

    @Column(name = "EMAIL", length = 50)
    private String email;

    @Column(name = "SALESMAN_CODE_OLD", length = 20)
    private String salesmanCodeOld;

    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "ALTERNATIVE_SALESMAN_USERID")
    private Long alternativeSalesmanUserId;

    @Column(name = "ALTERNATIVE_SALESMAN_EXPIRY")
    private LocalDate alternativeSalesmanExpiry;

    @Column(name = "EMPLOYEE_POID")
    private Long employeePoid;

    @Column(name = "SIGNATURE_TEXT", length = 1000)
    private String signatureText;

    @Column(name = "SALESMAN_SHORT_NAME", nullable = false, length = 25)
    private String salesmanShortName;
}
