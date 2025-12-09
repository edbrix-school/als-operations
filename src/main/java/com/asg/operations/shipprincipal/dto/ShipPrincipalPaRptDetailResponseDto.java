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
    
    @Schema(description = "Port call report type POID")
    private Long portCallReportTypePoid;
    
    @Schema(description = "Port call report type LOV details")
    private LovItem portCallReportTypeDet;
    
    @Schema(description = "PDF template POID")
    private Long pdfTemplatePoid;
    
    @Schema(description = "PDF template LOV details")
    private LovItem pdfTemplateDet;
    
    @Schema(description = "Email template POID")
    private Long emailTemplatePoid;
    
    @Schema(description = "Email template LOV details")
    private LovItem emailTemplateDet;
    
    @Schema(description = "Assigned to role POID")
    private Long assignedToRolePoid;
    
    @Schema(description = "Assigned to role LOV details")
    private LovItem assignedToRoleDet;
    
    @Schema(description = "Vessel type POID")
    private Long vesselTypePoid;
    
    @Schema(description = "Vessel type LOV details")
    private LovItem vesselTypeDet;
    
    @Schema(description = "Response time in hours")
    private Long responseTimeHrs;
    
    @Schema(description = "Frequency in hours")
    private Long frequencyHrs;
    
    @Schema(description = "Escalation role 1 POID")
    private Long escalationRole1Poid;
    
    @Schema(description = "Escalation role 1 LOV details")
    private LovItem escalationRole1Det;
    
    @Schema(description = "Escalation role 2 POID")
    private Long escalationRole2Poid;
    
    @Schema(description = "Escalation role 2 LOV details")
    private LovItem escalationRole2Det;
    
    @Schema(description = "Remarks")
    private String remarks;
}
