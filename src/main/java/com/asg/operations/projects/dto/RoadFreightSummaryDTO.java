package com.asg.operations.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoadFreightSummaryDTO {
    private Long detRowId;
    private Long jobId;
    private String jobNo;
    private String blAwbNumber;
    private String truckNumber;
    private String transportFrom;
    private String transportTo;
    private String origin;
    private LocalDate eta;
    private Double weight;
    private Double cbm;
    private String description;
    private String jobStatus;
    private String documentStatus;
    // Bayan/truck detail fields
    private String bayanNumber;
    private String bayanMode;
    private Double duty;
    private Double vat;
    private Double totalPaid;
    private LocalDate expiryDate;
    private LocalDate submittedDate;
    private LocalDate paymentDate;
    private LocalDate appointmentDate;
    private LocalDate deliveryDate;
    private String detention;
    private String remarks;
}
