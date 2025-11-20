package com.alsharif.operations.shipprincipal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "Principal creation request")
public class PrincipalCreateDTO {
    private Long groupPoid;
    private String principalCode;
    private String principalName;
    private String principalName2;
    private String tinNumber;
    private String taxSlab;
    private String exemptionReason;
    private Long glCodePoid;
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
    private boolean active;
    private String principalCodeOld;
    private Long countryPoid;

    @Schema(description = "Address information")
    private AddressMasterDTO address;
    
    @Schema(description = "List of charges")
    private List<ChargeItemDTO> charges;
    
    @Schema(description = "List of payment details")
    private List<PaymentItemDTO> payments;
    
    @Schema(description = "Create GL account flag", example = "true")
    private Boolean createGL;
}
