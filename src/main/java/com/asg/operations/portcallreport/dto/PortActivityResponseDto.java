package com.asg.operations.portcallreport.dto;

import com.asg.operations.commonlov.dto.LovItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortActivityResponseDto {
    private Long portActivityTypePoid;
    private String portActivityTypeCode;
    private String portActivityTypeName;
    private String portActivityTypeName2;
    private LovItem portActivityDet;
}
