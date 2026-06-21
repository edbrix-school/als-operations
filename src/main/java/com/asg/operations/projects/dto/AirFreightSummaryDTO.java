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
public class AirFreightSummaryDTO {
    private Long detRowId;
    private Long jobId;
    private String jobNo;
    private String mawbNo;
    private String hawbNo;
    private String flightNo;
    private String flightNo2;
    private String origin;
    private LovGetListDto originLov;
    private String destination;
    private LovGetListDto destinationLov;
    private String carrier;
    private LovGetListDto carrierLov;
    @JsonFormat(pattern = "dd-MMM-yy")
    private LocalDate etd;
    @JsonFormat(pattern = "dd-MMM-yy")
    private LocalDate eta;
    @JsonFormat(pattern = "dd-MMM-yy")
    private LocalDate actualArrivalDate;
    private Double noOfPackages;
    private Double weight;
    private Double cbm;
    private Double totalVolume;
    private Double chargeableWeight;
    private String description;
    private String jobStatus;
    private String documentStatus;
    @JsonFormat(pattern = "dd-MMM-yy")
    private LocalDate appointmentDate;
    @JsonFormat(pattern = "dd-MMM-yy")
    private LocalDate deliveryDate;
    private String imcoClassUnno;
    private String detention;
    private String remarks;
}
