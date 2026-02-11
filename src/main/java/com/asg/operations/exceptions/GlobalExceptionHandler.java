package com.asg.operations.exceptions;

import com.asg.operations.common.ApiResponse;
import com.asg.operations.crew.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.dao.DataAccessException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> handleAsgException(CustomException ex) {
        log.error("CustomException: {}", ex.getMessage());
        return ApiResponse.error(ex.getMessage(), ex.getCode());
    }

    @ExceptionHandler(jakarta.xml.bind.ValidationException.class)
    public ResponseEntity<?> handleValidationException(jakarta.xml.bind.ValidationException ex) {
        return ApiResponse.badRequest(ex.getMessage());
    }

    @ExceptionHandler(jakarta.validation.ValidationException.class)
    public ResponseEntity<?> handleValidationException(jakarta.validation.ValidationException ex) {
        return ApiResponse.badRequest(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleDateFormatException(MethodArgumentTypeMismatchException ex) {
        if (ex.getRequiredType() == LocalDate.class) {
            String value = ex.getValue() != null ? ex.getValue().toString() : null;
            String parameterName = ex.getPropertyName();

            String message = "Invalid date format. Please use YYYY-MM-DD.";
            if (StringUtils.isNotBlank(value) && StringUtils.isNotBlank(parameterName)) {
                message = String.format("Invalid value '%s' for parameter '%s'. Expected format: YYYY-MM-DD.", value, parameterName);
            }
            return ApiResponse.badRequest(message);
        }
        return ApiResponse.badRequest(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        log.info("Validation errors at {}", request.getRequestURI());
        return ApiResponse.error("Validation error occurred", HttpStatus.BAD_REQUEST.value(), errors);
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<?> handleMissingPathVariable(MissingPathVariableException ex) {
        String msg = String.format("Missing path variable: '%s'", ex.getVariableName());
        return ApiResponse.badRequest(msg);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<?> handleMissingHeader(MissingRequestHeaderException ex) {
        String msg = String.format("Missing Header variable: '%s'", ex.getHeaderName());
        return ApiResponse.badRequest(msg);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<?> handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {
        return ApiResponse.conflict(ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex) {
        return ApiResponse.notFound(ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFoundException(NoResourceFoundException ex) {
        return ApiResponse.notFound(ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleJsonParseErrors(HttpMessageNotReadableException ex) {
        Map<String, Object> response = new HashMap<>();

        // extract root cause if it’s IllegalArgumentException from enum
        Throwable cause = ex.getMostSpecificCause();
        String message = cause != null ? cause.getMessage() : "Invalid request payload";
        return ApiResponse.error(message, HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        String msg = "File exceeds the maximum allowed upload size. Please upload a smaller file.";
        log.warn("MaxUploadSizeExceededException: {}", ex.getMessage());
        return ApiResponse.badRequest(msg);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {} ", request.getRequestURI(), ex);
        return ApiResponse.error(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {} ", request.getRequestURI(), ex);
        return ApiResponse.error(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @ExceptionHandler(com.asg.operations.exceptions.ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(com.asg.operations.exceptions.ValidationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Validation Error",
                ex.getMessage(),
                ex.getFieldErrors()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<?> handleHandlerMethodValidationException(HandlerMethodValidationException ex, HttpServletRequest request) {

        Map<String, Object> errors = new HashMap<>();

        ex.getAllErrors().forEach(error -> {
            String fieldName = "unknown";

            if (error instanceof org.springframework.validation.FieldError fieldError) {
                fieldName = fieldError.getField();
            } else if (error.getCodes() != null && error.getCodes().length > 0) {
                // fallback: extract parameter name from validation codes
                fieldName = error.getCodes()[0];
            }

            errors.put(fieldName, error.getDefaultMessage());
        });

        log.info("Validation errors at {}", request.getRequestURI());

        return ApiResponse.error("Validation error occurred", HttpStatus.BAD_REQUEST.value(), errors);
    }

    @ExceptionHandler(com.asg.common.lib.exception.ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(com.asg.common.lib.exception.ValidationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Validation Error",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles Oracle trigger errors (ORA-20001, ORA-20002) from database triggers
     * These errors are thrown by PDA_FDA_HDR_GTTRG trigger for financial/transaction period validations
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<?> handleDataAccessException(DataAccessException ex, HttpServletRequest request) {
        Throwable rootCause = ex.getRootCause();
        
        if (rootCause instanceof SQLException sqlEx) {
            int errorCode = sqlEx.getErrorCode();
            String message = sqlEx.getMessage();
            
            // ORA-20001: Financial period validation error
            if (errorCode == 20001) {
                String userMessage = extractUserMessage(message, 
                    "Changes allowed only within current Financial Period. Please select a date within the allowed financial period.");
                log.warn("Financial period validation error at {}: {}", request.getRequestURI(), message);
                return ApiResponse.error(userMessage, 422);
            }
            
            // ORA-20002: Transaction period validation error
            if (errorCode == 20002) {
                String userMessage = extractUserMessage(message,
                    "Transaction date validation failed. Please ensure the date is within the allowed transaction period.");
                log.warn("Transaction period validation error at {}: {}", request.getRequestURI(), message);
                return ApiResponse.error(userMessage, 422);
            }
        }
        
        // For other database errors, log and return generic error
        log.error("DataAccessException at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ApiResponse.error("Database operation failed. Please contact support if the issue persists.", 
            HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    /**
     * Extracts user-friendly message from Oracle error message
     * Oracle error messages often contain technical details, we extract the meaningful part
     */
    private String extractUserMessage(String oracleMessage, String defaultMessage) {
        if (StringUtils.isBlank(oracleMessage)) {
            return defaultMessage;
        }
        
        // Try to extract meaningful error text after "ERROR:" or similar patterns
        String[] patterns = {
            "ERROR:",
            "error:",
            "Error:"
        };
        
        for (String pattern : patterns) {
            int index = oracleMessage.indexOf(pattern);
            if (index >= 0) {
                String extracted = oracleMessage.substring(index + pattern.length()).trim();
                // Remove trailing technical details if present
                if (extracted.contains("\n") || extracted.contains("\r")) {
                    extracted = extracted.split("[\n\r]")[0].trim();
                }
                if (StringUtils.isNotBlank(extracted)) {
                    return extracted;
                }
            }
        }
        
        // If no pattern found, return default message
        return defaultMessage;
    }


}
