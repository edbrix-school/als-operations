package com.alsharif.operations.shipprincipal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Address master information")
public class AddressMasterDTO {
    @Schema(description = "Group POID", example = "1")
    private Long groupPoid;
    
    @Schema(description = "Address name", example = "Main Office")
    private String addressName;
    
    @Schema(description = "Address name 2", example = "Secondary Name")
    private String addressName2;
    
    @Schema(description = "Preferred communication method", example = "EMAIL")
    private String preferredCommunication;
    
    @Schema(description = "Party type", example = "WCA")
    private String partyType;
    
    @Schema(description = "WhatsApp number", example = "+1234567890")
    private String whatsappNo;
    
    @Schema(description = "LinkedIn profile URL")
    private String linkedIn;
    
    @Schema(description = "Instagram profile URL")
    private String instagram;
    
    @Schema(description = "Facebook profile URL")
    private String facebook;
    
    @Schema(description = "Remarks")
    private String remarks;
    
    @Schema(description = "Country POID", example = "1")
    private Long countryPoid;
    
    @Schema(description = "CR number", example = "CR123456")
    private String crNumber;
    
    @Schema(description = "Active status", example = "Y")
    private String active;
    
    @Schema(description = "Is forwarder", example = "Y")
    private String isForwarder;
    
    @Schema(description = "List of address details")
    private List<AddressDetailDTO> details;
}
