package com.asg.operations.shipprincipal.dto;

import com.asg.operations.commonlov.dto.LovItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Charge item response information")
public class ChargeDetailResponseDto {

    @Schema(description = "Principal POID")
    private Long principalPoid;

    @Schema(description = "Detail row ID", example = "1")
    private Long detRowId;
    
    @Schema(description = "Charge POID", example = "100")
    private Long chargePoid;
    
    @Schema(description = "Charge information")
    private LovItem chargeDet;
    
    @Schema(description = "Rate", example = "150.50")
    private Long rate;
    
    @Schema(description = "Remarks")
    private String remarks;
}
