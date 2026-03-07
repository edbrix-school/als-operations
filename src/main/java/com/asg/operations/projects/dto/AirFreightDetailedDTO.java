package com.asg.operations.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirFreightDetailedDTO {
    private AirControlSheetDTO controlSheetInfo;
    private String flightNo;
    private LocalDateTime flightDate;
    private String flightNo2;
    private LocalDateTime flightDate2;
    private String originAirport;
    private String destinationAirport;
    private String airArrivalPort;
    private String airDeparturePort;
    private String airTransPort;
    private String airTransPort2;
    private String carrier;
    private String firstCarrier;
    private String secondCarrier;
    private String thirdCarrier;
    private String hawbNo;
    private LocalDate etd;
    private LocalDate eta;
    private LocalDate actualArrivalDate;
    private Long agentPoid;
    private String agentAcctNo;
    private String agentIataNo;
    private List<AirPackageDTO> packages;
    private List<BayanDTO> bayanDetails;
    private JobChargesDTO charges;
}
