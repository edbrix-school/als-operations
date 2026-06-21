package com.asg.operations.projects.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BayanDTO {
    private Long jobId;
    private String jobNo;
    private Long detRowId;
    private String blAwbNumber;
    private String bayanNumber;
    private String bayanMode;
    private BigDecimal dutyAmount;
    private BigDecimal vatAmount;
    private BigDecimal totalPaidAmount;
    @JsonFormat(pattern = "dd-MMM-yyyy")
    private LocalDate expiryDate;
    @JsonFormat(pattern = "dd-MMM-yyyy")
    private LocalDate submittedDate;
    @JsonFormat(pattern = "dd-MMM-yyyy")
    private LocalDate paymentDate;
}
