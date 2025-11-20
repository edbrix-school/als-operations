package com.alsharif.operations.shipprincipal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Address detail information")
public class AddressDetailDTO {
    @Schema(description = "Contact person name", example = "John Doe")
    private String contactPerson;
    
    @Schema(description = "Telephone number", example = "+1234567890")
    private String telephone;
    
    @Schema(description = "Email address", example = "contact@example.com")
    private String email;
    
    @Schema(description = "Fax number", example = "+1234567891")
    private String fax;
}
