package com.asg.operations.pdaRoRoVehicle.dto;

import com.asg.operations.pdaporttariffmaster.dto.GetAllTariffFilterRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllRoRoVehicleFilterRequest {
    private String from;
    private String to;
    private String operator;
    private String isDeleted;
    private List<FilterItem> filters;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterItem {
        private String searchField;
        private String searchValue;
    }
}
