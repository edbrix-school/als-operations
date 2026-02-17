package com.asg.operations.projects.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FFProjectsStoredProcRepository {

    private final EntityManager entityManager;

    /**
     * Loads quotation header and detail records for project creation
     * Stored Procedure: PROC_PROJECTS_QUOTATION_LOAD
     * 
     * @param quotationPoid Quotation reference POID
     * @return Map containing quotation header and charge details
     */
    public Map<String, Object> loadProjectsQuotation(Long quotationPoid) {
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("PROC_PROJECTS_QUOTATION_LOAD");
            
            query.registerStoredProcedureParameter("P_QUOTATION_POID", Long.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("P_HEADER_CURSOR", void.class, ParameterMode.REF_CURSOR);
            query.registerStoredProcedureParameter("P_DETAILS_CURSOR", void.class, ParameterMode.REF_CURSOR);
            
            query.setParameter("P_QUOTATION_POID", quotationPoid);
            query.execute();
            
            List<?> headerResult = (List<?>) query.getOutputParameterValue("P_HEADER_CURSOR");
            List<?> detailsResult = (List<?>) query.getOutputParameterValue("P_DETAILS_CURSOR");
            
            Map<String, Object> result = new HashMap<>();
            result.put("header", headerResult != null && !headerResult.isEmpty() ? headerResult.get(0) : null);
            result.put("chargeDetails", detailsResult);
            
            return result;
        } catch (Exception e) {
            log.error("Error loading quotation details for quotationPoid: {}", quotationPoid, e);
            return Map.of("error", "Failed to load quotation details: " + e.getMessage());
        }
    }

    /**
     * Loads and displays project related job details for control sheet
     * Stored Procedure: PROC_PROJECTS_JOBS_LOAD
     * 
     * @param transactionPoid Project transaction POID
     * @return Map containing job details for control sheet
     */
    public Map<String, Object> loadProjectsJobs(Long transactionPoid) {
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("PROC_PROJECTS_JOBS_LOAD");
            
            query.registerStoredProcedureParameter("P_TRANSACTION_POID", Long.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("P_JOBS_CURSOR", void.class, ParameterMode.REF_CURSOR);
            
            query.setParameter("P_TRANSACTION_POID", transactionPoid);
            query.execute();
            
            List<?> jobsResult = (List<?>) query.getOutputParameterValue("P_JOBS_CURSOR");
            
            Map<String, Object> result = new HashMap<>();
            result.put("jobs", jobsResult);
            
            return result;
        } catch (Exception e) {
            log.error("Error loading job details for transactionPoid: {}", transactionPoid, e);
            return Map.of("error", "Failed to load job details: " + e.getMessage());
        }
    }
}
