package com.asg.operations.shipprincipal.dto;

import com.asg.operations.commonlov.dto.LovItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
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
    @Size(max = 20, message = "Principal code cannot exceed 20 characters")
    private String principalCode;
    @Size(max = 100, message = "Principal name cannot exceed 100 characters")
    private String principalName;
    @Size(max = 100, message = "Principal name2 cannot exceed 100 characters")
    private String principalName2;
    private Long countryPoid;
    private LovItem countryDet;
    private Long addressPoid;
    private Long creditPeriod;
    private Long glCodePoid;
    private LovItem glCodeDet;
    @Size(max = 250, message = "Remarks cannot exceed 250 characters")
    private String remarks;
    @Size(max = 1, message = "Active cannot exceed 1 character")
    private String active;
    private Integer seqNo;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    @Size(max = 20, message = "Group name cannot exceed 20 characters")
    private String groupName;
    @Size(max = 20, message = "Gl Acct No cannot exceed 20 characters")
    private String glAcctNo;
    @Size(max = 20, message = "Principal code old cannot exceed 20 characters")
    private String principalCodeOld;
    @Size(max = 1, message = "Deleted cannot exceed 1 characters")
    private String deleted;
    private Long companyPoid;
    private LovItem companyDet;
    @Size(max = 20, message = "Currency code cannot exceed 20 characters")
    private String currencyCode;
    private BigDecimal currencyRate;
    private Long agreedPeriod;
    private BigDecimal buyingRate;
    private BigDecimal sellingRate;
    @Size(max = 100, message = "Tin number cannot exceed 100 characters")
    private String tinNumber;
    @Size(max = 100, message = "Tax Slab cannot exceed 100 characters")
    private String taxSlab;
    private LovItem taxSlabDet;
    @Size(max = 300, message = "Exemption Reason cannot exceed 300 characters")
    private String exemptionReason;

    @Schema(description = "List of charges")
    private List<ChargeDetailResponseDto> charges;

    @Schema(description = "List of payment details")
    private List<PaymentItemResponseDTO> payments;

    @Schema(description = "List of address details")
    private List<AddressDetailsDTO> addressDetails;

    @Schema(description = "List of port activity report details")
    private List<ShipPrincipalPaRptDetailResponseDto> portActivityReportDetails;
}
