package com.asg.operations.projects.projection;

import java.time.LocalDate;

public interface SeaFreightJobProjection {
    Long getDetRowId();
    Long getJobId();
    String getVessel();
    String getPol();
    String getPod();
    String getMblNo();
    String getHblNo();
    LocalDate getEtaAta();
    LocalDate getEtd();
    String getBooking();
    String getRelease();
    LocalDate getArrivalDate();
    LocalDate getSailDate();
    String getVoyageNo();
    String getBayanNumber();
    String getBayanMode();
    Double getDuty();
    Double getVat();
    Double getTotalPaid();
    LocalDate getExpiryDate();
    LocalDate getSubmittedDate();
    LocalDate getPaymentDate();
    String getContainerNo();
    String getContainerType();
    String getSealNumber();
    Integer getQtyPackages();
    Double getWeight();
    Double getCbm();
    LocalDate getAppointmentCollection();
    LocalDate getDeliveryDate();
    String getDetention();
    String getDestuffingFull();
    String getDocStatus();
    String getRemarks();
    String getJobStatus();
}
