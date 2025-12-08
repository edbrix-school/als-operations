package com.asg.operations.shipprincipal.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Ship principal port activity report detail request")
public class ShipPrincipalPaRptDetailDto {
    @Schema(description = "Detail row ID", example = "1")
    @Min(value = 1, message = "detRowId cannot be negative")
    private Long detRowId;
    
    @Schema(description = "Port call report type POID", example = "100")
    private Long portCallReportType;
    
    @Schema(description = "PDF template POID", example = "10")
    private Long pdfTemplatePoid;
    
    @Schema(description = "Email template POID", example = "20")
    private Long emailTemplatePoid;
    
    @Schema(description = "Assigned to role POID", example = "5")
    private Long assignedToRolePoid;
    
    @Schema(description = "Vessel type code", example = "131")
    private String vesselType;

    @Schema(description = "Response time in hours", example = "24")
    @Min(value = 0, message = "responseTimeHrs cannot be negative")
    private Long responseTimeHrs;

    @Schema(description = "Frequency in hours", example = "48")
    @Min(value = 0, message = "frequenceHrs cannot be negative")
    private Long frequenceHrs;

    @Schema(description = "Escalation role 1 POID", example = "6")
    private Long escalationRole1;
    
    @Schema(description = "Escalation role 2 POID", example = "7")
    private Long escalationRole2;
    
    @Schema(description = "Remarks", example = "Additional notes")
    @Size(max = 200, message = "Remarks cannot exceed 200 characters")
    private String remarks;
    
    @Schema(description = "Action type for update operations", example = "isCreated")
    private ActionType actionType;
}
