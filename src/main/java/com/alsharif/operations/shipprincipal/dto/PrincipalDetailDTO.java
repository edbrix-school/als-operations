package com.alsharif.operations.shipprincipal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Principal detail response")
public class PrincipalDetailDTO {
    private Long principalPoid;
    private Long groupPoid;
    private String principalCode;
    private String principalName;
    private String principalName2;
    private Long countryPoid;
    private Long addressPoid;
    private Long creditPeriod;
    private Long glCodePoid;
    private String remarks;
    private String active;
    private Integer seqNo;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private String groupName;
    private String glAcctNo;
    private String principalCodeOld;
    private String deleted;
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
    
    @Schema(description = "List of address details")
    private List<AddressDetailDTO> addressDetails;
}
