package com.asg.operations.pdaporttariffmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllTariffFilterRequest {
    private String from;
    private String to;
    private String operator; // "AND" or "OR"
    private String isDeleted; // "Y" or "N"
    private List<FilterItem> filters;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterItem {
        private String searchField; // e.g., "DOC_REF", "PORTS", "VESSEL_TYPES"
        private String searchValue; // text search value
    }
}