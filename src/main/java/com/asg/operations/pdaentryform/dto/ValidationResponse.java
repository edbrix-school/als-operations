package com.asg.operations.pdaentryform.dto;

import com.asg.operations.crew.dto.ValidationError;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for validation results
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationResponse {

    private boolean valid;
    private String status; // SUCCESS, ERROR, WARNING
    private String message;
    private List<ValidationError> errors;
    private List<String> warnings;
}

