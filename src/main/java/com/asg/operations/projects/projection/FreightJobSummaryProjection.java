package com.asg.operations.projects.projection;

import java.time.LocalDate;

public interface FreightJobSummaryProjection {
    Long getJobId();
    String getJobNo();
    String getFreightMode();
    String getLine();
    LocalDate getEtaAta();
    String getPol();
    String getPod();
    String getOrigin();
    String getDestination();
    String getDescription();
    Double getCbm();
    Double getPackages();
    Double getWeight();
    String getJobStatus();
    String getBlAwbNo();
    Long getPrincipalPoid();
}
