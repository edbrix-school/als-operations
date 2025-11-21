package com.alsharif.operations.commonlov.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;       // The list of records
    private int page;              // Current page number
    private int size;              // Page size
    private long totalElements;    // Total number of records
}
