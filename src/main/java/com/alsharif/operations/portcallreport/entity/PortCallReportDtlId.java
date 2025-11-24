package com.alsharif.operations.portcallreport.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortCallReportDtlId implements Serializable {
    private Long portCallReportPoid;
    private Long detRowId;
}
