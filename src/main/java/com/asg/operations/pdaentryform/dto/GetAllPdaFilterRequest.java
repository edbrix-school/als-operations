package com.asg.operations.pdaentryform.dto;

import lombok.Data;

import java.util.List;

@Data
public class GetAllPdaFilterRequest {
    private String from;
    private String to;
    private String operator;
    private String isDeleted;
    private List<FilterItem> filters;

    @Data
    public static class FilterItem {
        private String searchField;
        private String searchValue;
    }
}