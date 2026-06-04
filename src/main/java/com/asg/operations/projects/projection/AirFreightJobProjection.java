package com.asg.operations.projects.projection;

import java.time.LocalDate;

public interface AirFreightJobProjection {
    Long getJobId();
    String getJobNo();
    String getOrigin();
    String getDestination();
    LocalDate getEtd();
    LocalDate getEtaAta();
    Double getNoOfPackages();
    Double getWeight();
    Double getCbm();
    String getCarrierCode();
    String getDescription();
    String getJobStatus();
    String getDocumentStatus();
    String getFlightNo();
    String getHawbNo();
    String getMawbNo();
    Long getPrincipalPoid();
}
