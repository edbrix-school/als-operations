package com.asg.operations.projects.dto;

import com.asg.common.lib.dto.LovGetListDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobStatusPendingBillDTO {
    private Long detRowId;
    private Long jobId;
    private String jobNo;
    private String blNo;
    private LocalDate etaAta;
    private Long principalPoid;
    private LovGetListDto principalLov;
    private Long customerPoid;
    private LovGetListDto customerLov;
    private String mode;
    private String jobStatus;
    private LocalDate completedOn;
    private LocalDate compilationDate;
    private BigDecimal bookedAmount;
}
