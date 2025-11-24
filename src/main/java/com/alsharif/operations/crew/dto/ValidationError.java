package com.alsharif.operations.crew.dto;

import lombok.Data;

/**
 * DTO for field validation errors
 */
@Data
public class ValidationError {

    private Integer recordIndex; // For array validation errors
    private String field;
    private String message;

    public ValidationError() {
    }

    public ValidationError(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public ValidationError(Integer recordIndex, String field, String message) {
        this.recordIndex = recordIndex;
        this.field = field;
        this.message = message;
    }

}

