package com.asg.operations.projectjob.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProjectJobBayanDto {

    private Long detRowId;

    @Size(max = 50, message = "BL Awb Number must not exceed 50 characters")
    private String blAwbNumber;

    @Size(max = 50, message = "Bayan Number must not exceed 50 characters")
    private String bayanNumber;

    @Size(max = 50, message = "Bayan Mode must not exceed 50 characters")
    private String bayanMode;

    private BigDecimal dutyAmount;
    private BigDecimal vatAmount;
    private BigDecimal totalPaidAmount;

    private LocalDateTime expiryDate;
    private LocalDateTime submittedDate;
    private LocalDateTime paymentDate;

}