package com.alsharif.operations.shipprincipal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Address master information")
public class AddressMasterDTO {
    @Schema(description = "Group POID", example = "1")
    private Long groupPoid;

    @Schema(description = "List of address details")
    private List<AddressDetailDTO> details;
}
