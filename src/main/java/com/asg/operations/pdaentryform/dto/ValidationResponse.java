package com.asg.operations.pdaentryform.dto;

import com.asg.operations.crew.dto.ValidationError;

import java.util.List;

/**
 * Response DTO for validation results
 */
public class ValidationResponse {

    private boolean valid;
    private String status; // SUCCESS, ERROR, WARNING
    private String message;
    private List<ValidationError> errors;
    private List<String> warnings;

    public ValidationResponse() {
    }

    public ValidationResponse(boolean valid, String status, String message) {
        this.valid = valid;
        this.status = status;
        this.message = message;
    }

    // Getters and Setters

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationError> errors) {
        this.errors = errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}

