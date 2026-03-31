package com.asg.operations.projectjob.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProjectJobBayanDto {

    private Long detRowId;

    private String blAwbNumber;
    private String bayanNumber;
    private String bayanMode;

    private BigDecimal dutyAmount;
    private BigDecimal vatAmount;
    private BigDecimal totalPaidAmount;

    private LocalDateTime expiryDate;
    private LocalDateTime submittedDate;
    private LocalDateTime paymentDate;

    private String createdBy;
    private LocalDateTime createdDate;

    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;

}