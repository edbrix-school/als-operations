package com.asg.operations.shipprincipal.dto;

import com.asg.operations.commonlov.dto.LovItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Principal detail response")
public class PrincipalMasterDto {
    private Long principalPoid;
    private Long groupPoid;
    private String principalCode;
    private String principalName;
    private String principalName2;
    private Long countryPoid;
    private LovItem countryDet;
    private Long addressPoid;
    private Long creditPeriod;
    private Long glCodePoid;
    private LovItem glCodeDet;
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
    private LovItem companyDet;
    private String currencyCode;
    private BigDecimal currencyRate;
    private Long agreedPeriod;
    private BigDecimal buyingRate;
    private BigDecimal sellingRate;
    private String tinNumber;
    private String taxSlab;
    private String exemptionReason;
    
    @Schema(description = "List of charges")
    private List<ChargeDetailDto> charges;
    
    @Schema(description = "List of payment details")
    private List<PaymentItemResponseDTO> payments;
    
    @Schema(description = "List of address details")
    private List<AddressDetailsDTO> addressDetails;

    @Schema(description = "List of port activity report details")
    private List<ShipPrincipalPaRptDetailResponseDto> portActivityReportDetails;
}
