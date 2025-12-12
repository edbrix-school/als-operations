package com.asg.operations.pdaratetypemaster.dto;

import lombok.Data;
import java.util.List;

@Data
public class GetAllRateTypeFilterRequest {
    private String operator = "AND";
    private String isDeleted = "N";
    private List<FilterItem> filters;

    @Data
    public static class FilterItem {
        private String searchField;
        private String searchValue;
    }
}