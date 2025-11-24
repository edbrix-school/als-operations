package com.alsharif.operations.portactivitiesmaster.dto;

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