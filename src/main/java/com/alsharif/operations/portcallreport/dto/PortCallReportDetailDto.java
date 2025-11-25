package com.alsharif.operations.portcallreport.dto;

import com.alsharif.operations.portcallreport.enums.ActionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallReportDetailDto {
    private Long portCallReportPoid;
    private Long detRowId;
    @NotNull(message = "Port activity type is required")
    private Long portActivityTypePoid;
    private String portActivityTypeName;
    private String activityMandatory;
    private ActionType actionType;
}
