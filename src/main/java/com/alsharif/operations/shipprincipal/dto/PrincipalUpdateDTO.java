package com.alsharif.operations.shipprincipal.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PrincipalUpdateDTO {
    private Long groupPoid;
    private String principalCode;
    private String principalName;
    private String principalName2;
    private Long countryPoid;
    private Long creditPeriod;
    private String remarks;
    private String active;
    private Integer seqNo;
    private String groupName;
    private String principalCodeOld;
    private Long companyPoid;
    private String currencyCode;
    private BigDecimal currencyRate;
    private Long agreedPeriod;
    private BigDecimal buyingRate;
    private BigDecimal sellingRate;
    private String tinNumber;
    private String taxSlab;
    private String exemptionReason;
    
    private List<ChargeItemDTO> charges;
    private List<PaymentItemDTO> payments;
    private Boolean createGL;
}
