package com.asg.operations.projects.projection;

import java.time.LocalDate;

public interface AirFreightJobProjection {
    Long getDetRowId();
    Long getJobId();
    String getMawbNo();
    String getHawbNo();
    String getFlight();
    String getOrigin();
    String getDestination();
    String getCarrierCode();
    LocalDate getEtd();
    LocalDate getEtaAta();
    LocalDate getActualArrivalDate();
    String getBayanNumber();
    String getBayanMode();
    Double getDuty();
    Double getVat();
    Double getTotalPaid();
    LocalDate getExpiryDate();
    LocalDate getSubmittedDate();
    LocalDate getPaymentDate();
    String getDescription();
    Integer getNoOfPackages();
    Double getWeight();
    Double getCbm();
    LocalDate getAppointmentCollection();
    LocalDate getDeliveryDate();
    String getDetention();
    String getDocumentStatus();
    String getJobStatus();
    String getRemarks();
}
