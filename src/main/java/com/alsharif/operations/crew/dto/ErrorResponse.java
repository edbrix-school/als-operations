package com.alsharif.operations.crew.dto;

import java.util.List;

/**
 * Standard error response DTO
 */
public class ErrorResponse {

    private String error;
    private String message;
    private List<ValidationError> fieldErrors;

    public ErrorResponse() {
    }

    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
    }

    public ErrorResponse(String error, String message, List<ValidationError> fieldErrors) {
        this.error = error;
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

    // Getters and Setters
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ValidationError> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(List<ValidationError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}

