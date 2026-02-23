package com.asg.operations.finaldisbursementaccount.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;

/**
 * Repository for calling Oracle global validation functions
 * that check financial year and transaction period validity
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class GlobalValidationRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Validates if a date falls within the allowed financial year for a company
     * Calls FUNC_GLOB_FINANCIAL_YEAR_VALID which:
     * - Returns 'TRUE' if date is between FINANCIAL_PERIOD_START and FINANCIAL_PERION_END
     * - Returns 'ERROR' if date is outside the period
     * - Returns 'ERROR : ...' if an exception occurs
     *
     * @param companyPoid     Company identifier
     * @param transactionDate Date to validate
     * @return Result string - contains "ERROR" if validation fails, 'TRUE' if valid, null on exception
     */
    public String checkFinancialYearValid(Long companyPoid, LocalDate transactionDate) {
        if (companyPoid == null || transactionDate == null) {
            return null;
        }

        try {
            String sql = "SELECT FUNC_GLOB_FINANCIAL_YEAR_VALID(?, ?) FROM DUAL";
            String result = jdbcTemplate.queryForObject(sql, String.class, companyPoid, Date.valueOf(transactionDate));

            log.debug("Financial year validation for companyPoid={}, date={}: {}", companyPoid, transactionDate, result);

            return result;
        } catch (Exception e) {
            log.error("Error calling FUNC_GLOB_FINANCIAL_YEAR_VALID for companyPoid={}, date={}: {}", companyPoid, transactionDate, e.getMessage(), e);
            // If function call fails, we don't want to block the operation
            // Return null to indicate no error (fail-safe)
            return null;
        }
    }

    /**
     * Validates if a date falls within the allowed transaction period for a company
     * Calls FUNC_GLOB_TRANSACTN_YEAR_VALID which:
     * - Returns 'TRUE' if date is within the allowed transaction period
     * - Returns 'ERROR' if date is outside the period
     * - Returns 'ERROR : ...' if an exception occurs
     *
     * @param companyPoid     Company identifier
     * @param transactionDate Date to validate
     * @return Result string - contains "ERROR" if validation fails, 'TRUE' if valid, null on exception
     */
    public String checkTransactionYearValid(Long companyPoid, LocalDate transactionDate) {
        if (companyPoid == null || transactionDate == null) {
            return null;
        }

        try {
            String sql = "SELECT FUNC_GLOB_TRANSACTN_YEAR_VALID(?, ?) FROM DUAL";
            String result = jdbcTemplate.queryForObject(sql, String.class, companyPoid, Date.valueOf(transactionDate));

            log.debug("Transaction year validation for companyPoid={}, date={}: {}", companyPoid, transactionDate, result);

            return result;
        } catch (Exception e) {
            log.error("Error calling FUNC_GLOB_TRANSACTN_YEAR_VALID for companyPoid={}, date={}: {}", companyPoid, transactionDate, e.getMessage(), e);
            // If function call fails, we don't want to block the operation
            // Return null to indicate no error (fail-safe)
            return null;
        }
    }
}
