package com.alsharif.operations.shipprincipal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Address detail information")
public class AddressDetailDTO {

    @Schema(description = "Contact person name", example = "John Doe")
    private String contactPerson;
    
    @Schema(description = "Designation", example = "Manager")
    private String designation;
    
    @Schema(description = "Office telephone 1", example = "+1234567890")
    private String offTel1;
    
    @Schema(description = "Office telephone 2", example = "+1234567891")
    private String offTel2;
    
    @Schema(description = "Mobile number", example = "+1234567892")
    private String mobile;
    
    @Schema(description = "Email address 1", example = "contact@example.com")
    private String email1;
    
    @Schema(description = "Email address 2", example = "contact2@example.com")
    private String email2;
    
    @Schema(description = "Fax number", example = "+1234567893")
    private String fax;
    
    @Schema(description = "PO Box", example = "12345")
    private String poBox;
    
    @Schema(description = "Office number", example = "Suite 100")
    private String offNo;
    
    @Schema(description = "Building name", example = "Tower A")
    private String bldg;
    
    @Schema(description = "Road/Street", example = "Main Street")
    private String road;
    
    @Schema(description = "Area/City", example = "Downtown")
    private String areaCity;
    
    @Schema(description = "State", example = "California")
    private String state;
    
    @Schema(description = "Landmark/Remarks", example = "Near Central Park")
    private String landMark;
    
    @Schema(description = "WhatsApp number", example = "+1234567894")
    private String whatsappNo;
    
    @Schema(description = "LinkedIn profile URL")
    private String linkedIn;
    
    @Schema(description = "Instagram profile URL")
    private String instagram;
    
    @Schema(description = "Facebook profile URL")
    private String facebook;
}
