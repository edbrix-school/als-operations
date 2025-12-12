package com.asg.operations.shipprincipal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(max = 100, message = "Principal name cannot exceed 100 characters")
    @Schema(description = "Principal name", required = true)
    private String principalName;
    @Size(max = 100, message = "Principal name2 cannot exceed 100 characters")
    private String principalName2;
    @Size(max = 100, message = "Tin number cannot exceed 100 characters")
    private String tinNumber;
    @Size(max = 100, message = "Tax Slab cannot exceed 100 characters")
    private String taxSlab;
    @Size(max = 300, message = "Exemption Reason cannot exceed 300 characters")
    private String exemptionReason;
    private Long glCodePoid;
    @NotNull(message = "Company is required")
    @Schema(description = "Company POID", required = true)
    private Long companyPoid;
    @Size(max = 20, message = "Group name cannot exceed 20 characters")
    private String groupName;
    private Long creditPeriod;
    private Long agreedPeriod;
    @Size(max = 20, message = "Currency code cannot exceed 20 characters")
    private String currencyCode;
    private BigDecimal currencyRate;
    private BigDecimal buyingRate;
    private BigDecimal sellingRate;
    @Size(max = 200, message = "Remarks cannot exceed 200 characters")
    private String remarks;
    private Integer seqNo;
    @Pattern(regexp = "Y|N", message = "Active must be 'Y' or 'N'")
    @Size(max = 1, message = "Active cannot exceed 1 character")
    private String active;
    @Size(max = 20, message = "Principal code old cannot exceed 20 characters")
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
    
    @Schema(description = "List of port activity report details")
    private List<ShipPrincipalPaRptDetailDto> portActivityReportDetails;
}
