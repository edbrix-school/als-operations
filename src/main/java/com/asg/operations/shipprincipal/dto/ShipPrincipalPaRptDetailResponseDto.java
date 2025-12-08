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
@Schema(description = "Ship principal port activity report detail response")
public class ShipPrincipalPaRptDetailResponseDto {
    @Schema(description = "Detail row ID")
    private Long detRowId;
    
    @Schema(description = "Port call report type LOV details")
    private LovItem portCallReportType;
    
    @Schema(description = "PDF template LOV details")
    private LovItem pdfTemplate;
    
    @Schema(description = "Email template LOV details")
    private LovItem emailTemplate;
    
    @Schema(description = "Assigned to role LOV details")
    private LovItem assignedToRole;
    
    @Schema(description = "Vessel type LOV details")
    private LovItem vesselType;
    
    @Schema(description = "Response time in hours")
    private Long responseTimeHrs;
    
    @Schema(description = "Frequency in hours")
    private Long frequencyHrs;
    
    @Schema(description = "Escalation role 1 LOV details")
    private LovItem escalationRole1;
    
    @Schema(description = "Escalation role 2 LOV details")
    private LovItem escalationRole2;
    
    @Schema(description = "Remarks")
    private String remarks;
}
