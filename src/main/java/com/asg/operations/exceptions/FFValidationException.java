package com.asg.operations.exceptions;

import com.asg.operations.crew.dto.ValidationError;
import lombok.Getter;

import java.util.List;

@Getter
public class FFValidationException extends RuntimeException {

    private final List<ValidationError> errors;

    public FFValidationException(String message, List<ValidationError> errors) {
        super(message);
        this.errors = errors;
    }

    public FFValidationException(String message, List<ValidationError> errors, Throwable cause) {
        super(message, cause);
        this.errors = errors;
    }
}
