package com.asg.operations.shipprincipal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PrincipalListResponse {
    @JsonProperty("PRINCIPAL_POID")
    private Long principalPoid;

    @JsonProperty("GROUP_POID")
    private Long groupPoid;

    @JsonProperty("PRINCIPAL_CODE")
    private String principalCode;

    @JsonProperty("PRINCIPAL_NAME")
    private String principalName;

    @JsonProperty("PRINCIPAL_NAME2")
    private String principalName2;

    @JsonProperty("COUNTRY_POID")
    private Long countryPoid;

    @JsonProperty("ADDRESS_POID")
    private Long addressPoid;

    @JsonProperty("CREDIT_PERIOD")
    private Long creditPeriod;

    @JsonProperty("GL_CODE_POID")
    private Long glCodePoid;

    @JsonProperty("REMARKS")
    private String remarks;

    @JsonProperty("ACTIVE")
    private String active;

    @JsonProperty("SEQ_NO")
    private Integer seqNo;

    @JsonProperty("CREATED_BY")
    private String createdBy;

    @JsonProperty("CREATED_DATE")
    private LocalDateTime createdDate;

    @JsonProperty("LAST_MODIFIED_BY")
    private String lastModifiedBy;

    @JsonProperty("LAST_MODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @JsonProperty("DELETED")
    private String deleted;

    @JsonProperty("COMPANY_POID")
    private Long companyPoid;

    @JsonProperty("CURRENCY_CODE")
    private String currencyCode;

    @JsonProperty("CURRENCY_RATE")
    private BigDecimal currencyRate;

    @JsonProperty("AGREED_PERIOD")
    private Long agreedPeriod;

    @JsonProperty("BUYING_RATE")
    private BigDecimal buyingRate;

    @JsonProperty("SELLING_RATE")
    private BigDecimal sellingRate;

    @JsonProperty("TIN_NUMBER")
    private String tinNumber;

    @JsonProperty("TAX_SLAB")
    private String taxSlab;

    @JsonProperty("EXEMPTION_REASON")
    private String exemptionReason;
}