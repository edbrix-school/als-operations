package com.asg.operations.pdaratetypemaster.dto;

import java.util.List;

public class FormulaValidationResponse {

    private boolean valid;
    private List<String> errors;
    private List<String> warnings;
    private String normalizedFormula;
    private List<String> tokens;

    public FormulaValidationResponse() {
    }

    public FormulaValidationResponse(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = errors;
    }

    // Getters and Setters
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public String getNormalizedFormula() {
        return normalizedFormula;
    }

    public void setNormalizedFormula(String normalizedFormula) {
        this.normalizedFormula = normalizedFormula;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }
}
