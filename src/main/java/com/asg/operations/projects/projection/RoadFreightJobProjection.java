package com.asg.operations.projects.projection;

import java.time.LocalDate;

public interface RoadFreightJobProjection {
    Long getJobId();
    String getJobNo();
    String getBlAwbNumber();
    String getTransportFrom();
    String getTransportTo();
    LocalDate getEta();
    Double getWeight();
    Double getCbm();
    String getDescription();
    String getJobStatus();
    String getDocumentStatus();
}
