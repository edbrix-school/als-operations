package com.asg.operations.shipprincipal.dto;

import lombok.Data;
import java.util.List;

@Data
public class GetAllPrincipalFilterRequest {
    private String operator = "AND";
    private String isDeleted = "N";
    private List<FilterItem> filters;

    @Data
    public static class FilterItem {
        private String searchField;
        private String searchValue;
    }
}