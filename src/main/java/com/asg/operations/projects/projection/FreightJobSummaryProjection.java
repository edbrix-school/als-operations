package com.asg.operations.projects.projection;

import java.time.LocalDate;

public interface FreightJobSummaryProjection {
    Long getJobId();
    String getJobNo();
    String getFreightMode();
    String getLine();
    LocalDate getEtaAta();
    LocalDate getEtd();
    LocalDate getArrivalDate();
    LocalDate getSailDate();
    String getPol();
    String getPod();
    String getOrigin();
    String getDestination();
    String getDescription();
    Double getCbm();
    Double getPackages();
    Double getWeight();
    String getJobStatus();
    String getDocumentStatus();
    String getCarrierCode();
    String getVesselName();
    String getTransportFrom();
    String getTransportTo();
    String getBlAwbNo();
    Long getPrincipalPoid();
}
