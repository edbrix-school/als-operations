package com.asg.operations.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
public class ResourceAlreadyExistsException extends RuntimeException{

    private final String fieldName;
    private final String fieldValue;

    public ResourceAlreadyExistsException(String fieldName, String fieldValue) {
        super(String.format("%s already exists with value: %s", fieldName, fieldValue));
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
