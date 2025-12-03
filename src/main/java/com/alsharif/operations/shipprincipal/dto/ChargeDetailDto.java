package com.alsharif.operations.shipprincipal.dto;

import com.alsharif.operations.portcallreport.enums.ActionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Charge item information")
public class ChargeDetailDto {

    private Long principalPoid;

    @Schema(description = "Detail row ID", example = "1")
    @Min(value = 1, message = "detRowId cannot be negative")
    private Long detRowId;
    
    @Schema(description = "Charge POID", example = "100")
    private Long chargePoid;

    @Schema(description = "Charge Code", example = "100")
    private String chargeCode;

    @Schema(description = "Charge Name", example = "100")
    private String chargeName;
    
    @Schema(description = "Rate", example = "150.50")
    private Long rate;
    
    @Schema(description = "Remarks")
    @Size(max = 200, message = "Remarks cannot exceed 200 characters")
    private String remarks;

    private ActionType actionType;
}
