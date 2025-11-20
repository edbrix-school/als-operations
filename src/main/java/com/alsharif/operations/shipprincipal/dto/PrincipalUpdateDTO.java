package com.alsharif.operations.shipprincipal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "Principal update request")
public class PrincipalUpdateDTO {
    private Long groupPoid;
    private String principalCode;
    private String principalName;
    private String principalName2;
    private Long countryPoid;
    private Long creditPeriod;
    private String remarks;
    private boolean active;
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
    
    @Schema(description = "List of charges")
    private List<ChargeItemDTO> charges;
    
    @Schema(description = "List of payment details")
    private List<PaymentItemDTO> payments;
    
    @Schema(description = "Create GL account flag", example = "true")
    private Boolean createGL;
}
