package com.asg.operations.exceptions;



import com.asg.operations.crew.dto.ValidationError;

import java.util.List;

/**
 * Exception thrown when validation errors occur
 */
public class ValidationException extends RuntimeException {

    private final List<ValidationError> fieldErrors;

    public ValidationException(String message, List<ValidationError> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public ValidationException(String message, List<ValidationError> fieldErrors, Throwable cause) {
        super(message, cause);
        this.fieldErrors = fieldErrors;
    }

    public List<ValidationError> getFieldErrors() {
        return fieldErrors;
    }
}

