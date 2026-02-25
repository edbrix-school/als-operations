package com.asg.operations.projects.projection;

import java.time.LocalDate;

public interface FreightJobSummaryProjection {
    Long getDetRowId();
    Long getJobId();
    String getPrincipal();
    String getMode();
    LocalDate getEtaAta();
    String getPol();
    String getOrigin();
    String getDescription();
    String getBlAwbNo();
    Double getCbm();
    String getPackagesContainers();
    Double getWeight();
    String getJobStatus();
}
