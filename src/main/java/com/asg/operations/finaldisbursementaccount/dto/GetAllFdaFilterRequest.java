package com.asg.operations.finaldisbursementaccount.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllFdaFilterRequest {
    private String from;
    private String to;
    private String operator; // "AND" or "OR"
    private String isDeleted; // "Y" or "N"
    private List<FilterItem> filters;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterItem {
        private String searchField; // e.g., "VESSEL_NAME", "DOC_REF", "STATUS"
        private String searchValue; // text search value
    }
}