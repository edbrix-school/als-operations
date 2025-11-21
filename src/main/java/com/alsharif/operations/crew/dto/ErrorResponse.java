package com.alsharif.operations.crew.dto;

import lombok.Data;

import java.util.List;

/**
 * Standard error response DTO
 */
@Data
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

}

