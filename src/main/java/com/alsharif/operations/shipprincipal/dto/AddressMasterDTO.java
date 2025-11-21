package com.alsharif.operations.shipprincipal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Address master information")
public class AddressMasterDTO {
    @Schema(description = "Group POID", example = "1")
    private Long groupPoid;

    @Schema(description = "List of address details")
    private List<AddressDetailsDTO> details;
}
