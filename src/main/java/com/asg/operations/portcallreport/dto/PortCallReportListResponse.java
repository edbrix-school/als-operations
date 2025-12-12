package com.asg.operations.portcallreport.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PortCallReportListResponse {
    @JsonProperty("PORT_CALL_REPORT_POID")
    private Long portCallReportPoid;

    @JsonProperty("PORT_CALL_REPORT_ID")
    private String portCallReportId;

    @JsonProperty("PORT_CALL_REPORT_NAME")
    private String portCallReportName;

    @JsonProperty("PORT_CALL_APPL_VESSEL_TYPE")
    private String portCallApplVesselType;

    @JsonProperty("ACTIVE")
    private String active;

    @JsonProperty("SEQNO")
    private Long seqno;

    @JsonProperty("REMARKS")
    private String remarks;
}