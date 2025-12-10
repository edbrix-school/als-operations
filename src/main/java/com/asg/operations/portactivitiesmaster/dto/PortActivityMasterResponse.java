package com.asg.operations.portactivitiesmaster.dto;

import com.asg.operations.commonlov.dto.LovItem;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortActivityMasterResponse {
    private Long portActivityTypePoid;
    private Long groupPoid;
    private LovItem groupDet;
    private String portActivityTypeCode;
    private String portActivityTypeName;
    private String portActivityTypeName2;
    private String active;
    private Long seqno;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private String deleted;
    private String remarks;
}