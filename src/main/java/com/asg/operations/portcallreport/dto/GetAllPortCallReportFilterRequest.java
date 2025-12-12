package com.asg.operations.portcallreport.dto;

import lombok.Data;
import java.util.List;

@Data
public class GetAllPortCallReportFilterRequest {
    private String from;
    private String to;
    private String operator = "AND";
    private String isDeleted = "N";
    private List<FilterItem> filters;

    @Data
    public static class FilterItem {
        private String searchField;
        private String searchValue;
    }
}