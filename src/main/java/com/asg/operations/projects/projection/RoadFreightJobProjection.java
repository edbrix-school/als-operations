package com.asg.operations.projects.projection;

import java.time.LocalDate;

public interface RoadFreightJobProjection {
    Long getDetRowId();
    Long getJobId();
    String getBlAwbNumber();
    String getTruckNumber();
    String getBayanNumber();
    String getBayanMode();
    LocalDate getEta();
    Double getDuty();
    Double getVat();
    Double getTotalPaid();
    LocalDate getExpiryDate();
    LocalDate getSubmittedDate();
    LocalDate getPaymentDate();
    String getDocumentStatus();
    String getJobStatus();
}
