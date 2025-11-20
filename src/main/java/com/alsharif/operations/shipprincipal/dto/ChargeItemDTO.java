package com.alsharif.operations.shipprincipal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Charge item information")
public class ChargeItemDTO {
    @Schema(description = "Detail row ID", example = "1")
    private Long detRowId;
    
    @Schema(description = "Charge POID", example = "100")
    private Long chargePoid;
    
    @Schema(description = "Rate", example = "150.50")
    private BigDecimal rate;
    
    @Schema(description = "Remarks")
    private String remarks;
}
