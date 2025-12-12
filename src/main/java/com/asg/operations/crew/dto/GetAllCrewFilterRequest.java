package com.asg.operations.crew.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllCrewFilterRequest {
    private String from;
    private String to;
    private String operator; // "AND" or "OR"
    private String isDeleted; // "Y" or "N"
    private List<FilterItem> filters;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterItem {
        private String searchField; // e.g., "CREW_NAME", "NATIONALITY", "COMPANY"
        private String searchValue; // text search value
    }
}