package com.asg.operations.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TruckCargoDTO {
    private Long detRowId;
    private String blAwbNumber;
    private String truckNumber;
    private String bayanNumber;
    private String bayanCode;
    private LocalDate eta;
    private BigDecimal dutyAmount;
    private BigDecimal vatAmount;
    private BigDecimal totalPaidAmount;
    private LocalDate expiryDate;
    private LocalDate submittedDate;
    private LocalDate paymentDate;
    private String documentStatus;
    private String remarks;
}
