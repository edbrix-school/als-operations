package com.alsharif.operations.shipprincipal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Principal creation request")
public class PrincipalCreateDTO {
    private String principalCode;
    
    @NotBlank(message = "Principal Name is mandatory")
    @Schema(description = "Principal name", required = true)
    private String principalName;
    
    private String principalName2;
    private String tinNumber;
    private String taxSlab;
    private String exemptionReason;
    private Long glCodePoid;
    
    @NotNull(message = "Company is required")
    @Schema(description = "Company POID", required = true)
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
    private Long addressPoid;
    private String addressName;

    @Schema(description = "Address information")
    private AddressTypeMapDTO addressTypeMap;
    
    @Schema(description = "List of charges")
    private List<ChargeDetailDto> charges;
    
    @Schema(description = "List of payment details")
    private List<PaymentItemDTO> payments;
}
