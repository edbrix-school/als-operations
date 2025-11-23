package com.alsharif.operations.crew.dto;

import lombok.Data;

import java.util.List;

/**
 * Generic paginated response DTO
 */
@Data
public class PageResponse<T> {

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;
    private int numberOfElements;

    public PageResponse() {
    }

    public PageResponse(List<T> content, int pageNumber, int pageSize, long totalElements) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
        this.last = pageNumber >= totalPages - 1;
        this.first = pageNumber == 0;
        this.numberOfElements = content != null ? content.size() : 0;
    }

}

