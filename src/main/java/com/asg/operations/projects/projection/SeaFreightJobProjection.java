package com.asg.operations.projects.projection;

import java.time.LocalDate;

public interface SeaFreightJobProjection {
    Long getJobId();
    String getJobNo();
    String getPol();
    String getPod();
    LocalDate getEtd();
    LocalDate getEtaAta();
    LocalDate getArrivalDate();
    LocalDate getSailDate();
    Double getWeight();
    Double getCbm();
    Long getLine();
    String getVesselName();
    String getMasterBlNo();
    String getHouseBlNo();
    String getDescription();
    String getJobStatus();
    String getDocumentStatus();
}
