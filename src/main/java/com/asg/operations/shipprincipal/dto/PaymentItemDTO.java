package com.asg.operations.shipprincipal.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payment item information")
public class PaymentItemDTO {

    @Schema(description = "Principal Poid", example = "1")
    private Long principalPoid;

    @Schema(description = "Detail row ID", example = "1")
    private Long detRowId;

    @Schema(description = "Payment type", example = "BANK_TRANSFER")
    @Size(max = 30, message = "Type cannot exceed 30 characters")
    private String type;

    @Schema(description = "Beneficiary name", example = "ABC Company")
    @Size(max = 50, message = "Beneficiary name cannot exceed 50 characters")
    private String beneficiaryName;

    @Schema(description = "Address")
    @Size(max = 250, message = "Address cannot exceed 250 characters")
    private String address;

    @Schema(description = "Bank name", example = "Bank of America")
    @Size(max = 100, message = "Bank name cannot exceed 100 characters")
    private String bank;

    @Schema(description = "Bank address")
    @Size(max = 250, message = "Bank address cannot exceed 250 characters")
    private String bankAddress;

    @Schema(description = "Beneficiary country POID", example = "1")
    private Long beneficiaryCountry;

    @Schema(description = "SWIFT code", example = "BOFAUS3N")
    @Size(max = 50, message = "SWIFT code cannot exceed 50 characters")
    private String swiftCode;

    @Schema(description = "Account number", example = "1234567890")
    @Size(max = 50, message = "Account number cannot exceed 50 characters")
    private String accountNumber;

    @Schema(description = "IBAN", example = "GB82WEST12345698765432")
    @Size(max = 100, message = "IBAN cannot exceed 100 characters")
    private String iban;

    @Schema(description = "Intermediary bank")
    @Size(max = 100, message = "Intermediary bank cannot exceed 100 characters")
    private String intermediaryBank;

    @Schema(description = "Intermediary account")
    @Size(max = 50, message = "Intermediary account cannot exceed 50 characters")
    private String intermediaryAcct;

    @Schema(description = "Bank SWIFT code")
    @Size(max = 50, message = "Bank SWIFT code cannot exceed 50 characters")
    private String bankSwiftCode;

    @Schema(description = "Intermediary country POID", example = "1")
    private Long intermediaryCountryPoid;

    @Schema(description = "Active status")
    @Size(max = 1, message = "Active status cannot exceed 1 character")
    private String active;

    @Schema(description = "Default payment", example = "N")
    @Size(max = 1, message = "Default payment cannot exceed 1 character")
    private String defaults;

    @Schema(description = "Remarks")
    @Size(max = 250, message = "Remarks cannot exceed 250 characters")
    private String remarks;

    @Schema(description = "Beneficiary ID")
    @Size(max = 25, message = "Beneficiary ID cannot exceed 25 characters")
    private String beneficiaryId;

    @Schema(description = "Intermediary other details")
    @Size(max = 50, message = "Intermediary other details cannot exceed 50 characters")
    private String intermediaryOth;

    @Schema(description = "Special instructions")
    @Size(max = 250, message = "Special instructions cannot exceed 250 characters")
    private String specialInstruction;

    private ActionType actionType;
}
