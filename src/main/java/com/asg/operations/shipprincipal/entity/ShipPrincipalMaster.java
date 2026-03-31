package com.asg.operations.shipprincipal.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "SHIP_PRINCIPAL_MASTER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipPrincipalMaster extends BaseEntity {

    @Id
    @Column(name = "PRINCIPAL_POID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long principalPoid;

    @Column(name = "PRINCIPAL_CODE", length = 20, unique = true, nullable = false)
    private String principalCode;

    @Column(name = "PRINCIPAL_NAME", length = 100, unique = true, nullable = false)
    private String principalName;

    @Column(name = "PRINCIPAL_NAME2", length = 100)
    private String principalName2;

    @AuditIgnore
    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @AuditIgnore
    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "GROUP_NAME", length = 20)
    private String groupName;

    @Column(name = "COUNTRY_POID")
    private Long countryPoid;

    @Column(name = "ADDRESS_POID")
    private Long addressPoid;

    @Column(name = "CREDIT_PERIOD")
    private Long creditPeriod;

    @Column(name = "AGREED_PERIOD")
    private Long agreedPeriod;

    @Column(name = "CURRENCY_CODE", length = 20)
    private String currencyCode;

    @Column(name = "CURRENCY_RATE")
    private BigDecimal currencyRate;

    @Column(name = "BUYING_RATE")
    private BigDecimal buyingRate;

    @Column(name = "SELLING_RATE")
    private BigDecimal sellingRate;

    @Column(name = "GL_CODE_POID")
    private Long glCodePoid;

    @Column(name = "GL_ACCTNO", length = 20)
    private String glAcctno;

    @Column(name = "TIN_NUMBER", length = 100)
    private String tinNumber;

    @Column(name = "TAX_SLAB", length = 100)
    private String taxSlab;

    @Column(name = "EXEMPTION_REASON", length = 300)
    private String exemptionReason;

    @Column(name = "REMARKS", length = 250)
    private String remarks;

    @Column(name = "SEQNO", precision = 5)
    private Integer seqno;

    @Column(name = "ACTIVE", length = 1)
    private String active;

    @Column(name = "PRINCIPAL_CODE_OLD", length = 20)
    private String principalCodeOld;

    @AuditIgnore
    @Column(name = "DELETED", length = 1)
    private String deleted;

    @PrePersist
    protected void onCreate() {
        if (deleted == null) {
            deleted = "N";
        }
        if (active == null) {
            active = "Y";
        }
    }
}