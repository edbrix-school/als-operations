package com.alsharif.operations.crew.util;


import com.alsharif.operations.crew.dto.ContractCrewDtlRequest;
import com.alsharif.operations.crew.dto.ValidationError;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for business validation logic
 */
public class ValidationUtil {

    /**
     * Validate passport dates
     * Rule: Passport expiry date must not be earlier than passport issue date
     */
    public static ValidationError validatePassportDates(LocalDate issueDate, LocalDate expiryDate) {
        if (issueDate != null && expiryDate != null) {
            if (expiryDate.isBefore(issueDate)) {
                return new ValidationError(
                    "crewPassportExpiryDate",
                    "Passport expiry date cannot be earlier than issue date"
                );
            }
        }
        return null;
    }

    /**
     * Validate document dates for detail records
     * Rules:
     * 1. Document issue date must not be earlier than document applied date
     * 2. Document expiry date must not be earlier than document issue date
     */
    public static List<ValidationError> validateDocumentDates(
            ContractCrewDtlRequest detail,
            Integer recordIndex
    ) {
        List<ValidationError> errors = new ArrayList<>();

        LocalDate appliedDate = detail.getDocumentAppliedDate();
        LocalDate issueDate = detail.getDocumentIssueDate();
        LocalDate expiryDate = detail.getDocumentExpiryDate();

        // Rule 1: Issue date must not be earlier than applied date
        if (appliedDate != null && issueDate != null) {
            if (issueDate.isBefore(appliedDate)) {
                errors.add(new ValidationError(
                    recordIndex,
                    "documentIssueDate",
                    "Document issue date cannot be earlier than applied date"
                ));
            }
        }

        // Rule 2: Expiry date must not be earlier than issue date
        if (issueDate != null && expiryDate != null) {
            if (expiryDate.isBefore(issueDate)) {
                errors.add(new ValidationError(
                    recordIndex,
                    "documentExpiryDate",
                    "Document expiry date cannot be earlier than issue date"
                ));
            }
        }

        return errors;
    }

    /**
     * Validate all detail records in bulk save request
     */
    public static List<ValidationError> validateAllDetailDates(
            List<ContractCrewDtlRequest> details
    ) {
        List<ValidationError> errors = new ArrayList<>();

        for (int i = 0; i < details.size(); i++) {
            ContractCrewDtlRequest detail = details.get(i);
            List<ValidationError> detailErrors = validateDocumentDates(detail, i);
            errors.addAll(detailErrors);
        }

        return errors;
    }
}

