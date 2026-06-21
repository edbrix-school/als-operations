package com.asg.operations.projects.dto;

import com.asg.common.lib.dto.LovGetListDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingJobDTO {
    private Long detRowId;
    private Long jobId;
    private String jobNo;
    private String blAwbNo;
    private String freightType;
    private String line;
    private LovGetListDto lineLov;
    @JsonFormat(pattern = "dd-MMM-yy")
    private LocalDate eta;
    @JsonFormat(pattern = "dd-MMM-yy")
    private LocalDate etd;
    private String pol;
    private LovGetListDto polLov;
    private String pod;
    private String origin;
    private LovGetListDto originLov;
    private String destination;
    private LovGetListDto destinationLov;
    private String description;
    private Double cbm;
    private Double packages;
    private Double weight;
    private String jobStatus;
    private Boolean canCreateJob;
    private String active;
}
