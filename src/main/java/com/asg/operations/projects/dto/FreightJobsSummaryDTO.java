package com.asg.operations.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FreightJobsSummaryDTO {
    private List<FreightSummaryDTO> allFreights;
    private List<AirFreightSummaryDTO> airFreights;
    private List<SeaFreightSummaryDTO> seaFreights;
    private List<RoadFreightSummaryDTO> roadFreights;
    private List<UpcomingJobDTO> upcomingJobs;
    private SummaryTotalsDTO totals;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryTotalsDTO {
        private Integer totalJobs;
        private Integer airCount;
        private Integer seaCount;
        private Integer roadCount;
        private Integer upcomingCount;
        private Double totalWeight;
        private Double totalCbm;
    }
}
