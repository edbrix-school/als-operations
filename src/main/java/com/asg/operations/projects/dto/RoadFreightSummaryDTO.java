package com.asg.operations.projects.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "dd-MMM-yy")
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
    @JsonFormat(pattern = "dd-MMM-yy")
    private LocalDate expiryDate;
    @JsonFormat(pattern = "dd-MMM-yy")
    private LocalDate submittedDate;
    @JsonFormat(pattern = "dd-MMM-yy")
    private LocalDate paymentDate;
    @JsonFormat(pattern = "dd-MMM-yy")
    private LocalDate appointmentDate;
    @JsonFormat(pattern = "dd-MMM-yy")
    private LocalDate deliveryDate;
    private String detention;
    private String remarks;
}
