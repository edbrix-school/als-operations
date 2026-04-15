package com.asg.operations.projectjob.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProjectJobTruckDto {

    private Long detRowId;

    @Size(max = 50, message = "Bl Awb Number must not exceed 50 characters")
    private String blAwbNumber;

    @Size(max = 50, message = "Bayan Number must not exceed 50 characters")
    private String bayanNumber;

    @Size(max = 50, message = "Bayan Code must not exceed 50 characters")
    private String bayanCode;

    private LocalDateTime eta;

    private BigDecimal dutyAmount;
    private BigDecimal vatAmount;
    private BigDecimal totalPaidAmount;

    private LocalDateTime expiryDate;
    private LocalDateTime submittedDate;
    private LocalDateTime paymentDate;

    @Size(max = 50, message = "Document Status must not exceed 50 characters")
    private String documentStatus;

    @Size(max = 100, message = "Truck Number must not exceed 50 characters")
    private String truckNumber;
}
