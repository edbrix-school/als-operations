package com.asg.operations.portactivitiesmaster.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PortActivityListResponse {
    @JsonProperty("PORT_ACTIVITY_TYPE_POID")
    private Long portActivityTypePoid;

    @JsonProperty("GROUP_POID")
    private Long groupPoid;

    @JsonProperty("PORT_ACTIVITY_TYPE_CODE")
    private String portActivityTypeCode;

    @JsonProperty("PORT_ACTIVITY_TYPE_NAME")
    private String portActivityTypeName;

    @JsonProperty("PORT_ACTIVITY_TYPE_NAME2")
    private String portActivityTypeName2;

    @JsonProperty("ACTIVE")
    private String active;

    @JsonProperty("SEQNO")
    private Long seqno;

    @JsonProperty("CREATED_BY")
    private String createdBy;

    @JsonProperty("CREATED_DATE")
    private LocalDateTime createdDate;

    @JsonProperty("LAST_MODIFIED_BY")
    private String lastModifiedBy;

    @JsonProperty("LAST_MODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @JsonProperty("DELETED")
    private String deleted;

    @JsonProperty("REMARKS")
    private String remarks;
}