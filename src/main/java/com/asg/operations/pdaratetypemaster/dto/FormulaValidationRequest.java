package com.asg.operations.pdaratetypemaster.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FormulaValidationRequest {
    @NotBlank(message = "Formula is mandatory")
    @Size(max = 1000, message = "Formula must not exceed 1000 characters")
    private String formula;

    private List<String> referencedTokens;
    private FormulaContext context;

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
