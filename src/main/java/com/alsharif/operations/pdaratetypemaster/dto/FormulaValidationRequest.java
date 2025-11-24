package com.alsharif.operations.pdaratetypemaster.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class FormulaValidationRequest {
    @NotBlank(message = "Formula is mandatory")
    @Size(max = 1000, message = "Formula must not exceed 1000 characters")
    private String formula;

    private List<String> referencedTokens;
    private FormulaContext context;

    // Getters and Setters
    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public List<String> getReferencedTokens() {
        return referencedTokens;
    }

    public void setReferencedTokens(List<String> referencedTokens) {
        this.referencedTokens = referencedTokens;
    }

    public FormulaContext getContext() {
        return context;
    }

    public void setContext(FormulaContext context) {
        this.context = context;
    }

    /**
     * Inner class for formula validation context
     */
    public static class FormulaContext {
        private List<String> allowableTokens;
        private Integer maxLength;

        public List<String> getAllowableTokens() {
            return allowableTokens;
        }

        public void setAllowableTokens(List<String> allowableTokens) {
            this.allowableTokens = allowableTokens;
        }

        public Integer getMaxLength() {
            return maxLength;
        }

        public void setMaxLength(Integer maxLength) {
            this.maxLength = maxLength;
        }
    }
}
