package com.alsharif.operations.crew.dto;

/**
 * DTO for field validation errors
 */
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

    // Getters and Setters
    public Integer getRecordIndex() {
        return recordIndex;
    }

    public void setRecordIndex(Integer recordIndex) {
        this.recordIndex = recordIndex;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

