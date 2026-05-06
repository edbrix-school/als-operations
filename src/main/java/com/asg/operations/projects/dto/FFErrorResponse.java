package com.asg.operations.projects.dto;

import com.asg.operations.crew.dto.ValidationError;
import lombok.Data;

import java.util.List;

@Data
public class FFErrorResponse {

    private String error;
    private String message;
    private List<ValidationError> errors;

    public FFErrorResponse(String error, String message, List<ValidationError> errors) {
        this.error = error;
        this.message = message;
        this.errors = errors;
    }
}
