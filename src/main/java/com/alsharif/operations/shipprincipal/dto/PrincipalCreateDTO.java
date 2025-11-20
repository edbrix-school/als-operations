package com.alsharif.operations.shipprincipal.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PrincipalCreateDTO {
    private Long groupPoid;
    private String principalCode;
    private String principalName;
    private String principalName2;
    private String tinNumber;
    private String taxSlab;
    private String exemptionReason;
    private Long glPoid;
    private Long companyPoid;
    private String groupName;
    private Long creditPeriod;
    private Long agreedPeriod;
    private String currencyCode;
    private BigDecimal currencyRate;
    private BigDecimal buyingRate;
    private BigDecimal sellingRate;
    private String remarks;
    private Integer seqNo;
    private String active;
    private String principalCodeOld;
    private Long countryPoid;

    private AddressMasterDTO address;
    private List<ChargeItemDTO> charges;
    private List<PaymentItemDTO> payments;
    private Boolean createGL;
}
