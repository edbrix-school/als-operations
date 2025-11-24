package com.alsharif.operations.common.Util;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class FormulaValidator {

    private static final List<String> DEFAULT_ALLOWABLE_TOKENS = Arrays.asList(
            "GRT", "NRT", "UNIT", "DAYS", "RATE", "BASE_QTY", "QTY", "AMOUNT"
    );

    // Allowed operators
    private static final List<String> ALLOWED_OPERATORS = Arrays.asList(
            "+", "-", "*", "/", "(", ")", "=", "<", ">", "<=", ">=", "!="
    );

    /**
     * Validate formula syntax and tokens
     */
    public FormulaValidationResult validate(String formula, List<String> allowableTokens) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> extractedTokens = new ArrayList<>();

        if (formula == null || formula.trim().isEmpty()) {
            return new FormulaValidationResult(true, errors, warnings, formula, extractedTokens);
        }

        String normalizedFormula = formula.trim();

        // Check length
        if (normalizedFormula.length() > 1000) {
            errors.add("Formula exceeds maximum length of 1000 characters");
            return new FormulaValidationResult(false, errors, warnings, normalizedFormula, extractedTokens);
        }

        // Use default allowable tokens if not provided
        if (allowableTokens == null || allowableTokens.isEmpty()) {
            allowableTokens = DEFAULT_ALLOWABLE_TOKENS;
        }

        // Extract tokens (identifiers that match pattern [A-Z_][A-Z0-9_]*)
        Pattern tokenPattern = Pattern.compile("\\b([A-Z][A-Z0-9_]*)\\b");
        Matcher tokenMatcher = tokenPattern.matcher(normalizedFormula.toUpperCase());

        Set<String> foundTokens = new HashSet<>();
        while (tokenMatcher.find()) {
            String token = tokenMatcher.group(1);
            // Skip if it's a number or known operator
            if (!isNumeric(token) && !isOperator(token)) {
                foundTokens.add(token);
                extractedTokens.add(token);
            }
        }

        // Validate each token
        for (String token : foundTokens) {
            if (!allowableTokens.contains(token)) {
                errors.add("Invalid token found: '" + token + "'. Allowed tokens: " + allowableTokens);
            }
        }

        // Basic syntax validation (balanced parentheses)
        if (!isBalancedParentheses(normalizedFormula)) {
            errors.add("Unbalanced parentheses in formula");
        }

        // Check for invalid characters (allow alphanumeric, spaces, operators, parentheses, decimal points)
        Pattern validCharPattern = Pattern.compile("^[A-Z0-9_\\s+\\-*/().=<>,!]+$", Pattern.CASE_INSENSITIVE);
        if (!validCharPattern.matcher(normalizedFormula).matches()) {
            errors.add("Formula contains invalid characters");
        }

        // Check for consecutive operators (basic validation)
        Pattern consecutiveOpsPattern = Pattern.compile("[+\\-*/]{2,}");
        if (consecutiveOpsPattern.matcher(normalizedFormula).find()) {
            warnings.add("Formula contains consecutive operators - verify syntax");
        }

        boolean isValid = errors.isEmpty();
        return new FormulaValidationResult(isValid, errors, warnings, normalizedFormula, extractedTokens);
    }

    /**
     * Check if string is numeric
     */
    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check if string is an operator
     */
    private boolean isOperator(String str) {
        return ALLOWED_OPERATORS.contains(str);
    }

    /**
     * Check if parentheses are balanced
     */
    private boolean isBalancedParentheses(String formula) {
        int count = 0;
        for (char c : formula.toCharArray()) {
            if (c == '(') {
                count++;
            } else if (c == ')') {
                count--;
                if (count < 0) {
                    return false;
                }
            }
        }
        return count == 0;
    }

    /**
     * Result class for formula validation
     */
    public static class FormulaValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;
        private final String normalizedFormula;
        private final List<String> tokens;

        public FormulaValidationResult(boolean valid, List<String> errors, List<String> warnings,
                                       String normalizedFormula, List<String> tokens) {
            this.valid = valid;
            this.errors = errors != null ? errors : new ArrayList<>();
            this.warnings = warnings != null ? warnings : new ArrayList<>();
            this.normalizedFormula = normalizedFormula;
            this.tokens = tokens != null ? tokens.stream().distinct().collect(Collectors.toList()) : new ArrayList<>();
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public String getNormalizedFormula() {
            return normalizedFormula;
        }

        public List<String> getTokens() {
            return tokens;
        }
    }
}
