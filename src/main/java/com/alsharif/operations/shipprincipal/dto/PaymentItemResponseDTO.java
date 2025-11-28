package com.alsharif.operations.shipprincipal.dto;

import com.alsharif.operations.commonlov.dto.LovItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payment item response information")
public class PaymentItemResponseDTO {

    @Schema(description = "Principal Poid", example = "1")
    private Long principalPoid;

    @Schema(description = "Detail row ID", example = "1")
    private Long detRowId;
    
    @Schema(description = "Payment type")
    private LovItem type;
    
    @Schema(description = "Beneficiary name", example = "ABC Company")
    private String beneficiaryName;
    
    @Schema(description = "Address")
    private String address;
    
    @Schema(description = "Bank name", example = "Bank of America")
    private String bank;
    
    @Schema(description = "Bank address")
    private String bankAddress;
    
    @Schema(description = "Beneficiary country POID", example = "1")
    private Long beneficiaryCountry;
    
    @Schema(description = "SWIFT code", example = "BOFAUS3N")
    private String swiftCode;
    
    @Schema(description = "Account number", example = "1234567890")
    private String accountNumber;
    
    @Schema(description = "IBAN", example = "GB82WEST12345698765432")
    private String iban;
    
    @Schema(description = "Intermediary bank")
    private String intermediaryBank;
    
    @Schema(description = "Intermediary account")
    private String intermediaryAcct;
    
    @Schema(description = "Bank SWIFT code")
    private String bankSwiftCode;
    
    @Schema(description = "Intermediary country POID", example = "1")
    private Long intermediaryCountryPoid;
    
    @Schema(description = "Active status")
    private String active;
    
    @Schema(description = "Default payment", example = "N")
    private String defaults;
    
    @Schema(description = "Remarks")
    private String remarks;
    
    @Schema(description = "Beneficiary ID")
    private String beneficiaryId;
    
    @Schema(description = "Intermediary other details")
    private String intermediaryOth;
    
    @Schema(description = "Special instructions")
    private String specialInstruction;
}
