package com.asg.operations.projects.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.ArrayList;
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
     * @param quoteId Quotation reference POID
     * @param suppQuoteFlag Supplementary quote flag (Y/N)
     * @return Map containing status, quotation header and charge details
     */
    public Map<String, Object> loadProjectsQuotation(Long quoteId, String suppQuoteFlag) {
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("PROC_PROJECTS_QUOTATION_LOAD");
            
            query.registerStoredProcedureParameter(1, Long.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(3, String.class, ParameterMode.OUT);
            query.registerStoredProcedureParameter(4, void.class, ParameterMode.REF_CURSOR);
            query.registerStoredProcedureParameter(5, void.class, ParameterMode.REF_CURSOR);
            
            query.setParameter(1, quoteId);
            query.setParameter(2, suppQuoteFlag);
            query.execute();
            
            String status = (String) query.getOutputParameterValue(3);
            ResultSet headerRs = (ResultSet) query.getOutputParameterValue(4);
            ResultSet detailRs = (ResultSet) query.getOutputParameterValue(5);
            
            List<Map<String, Object>> headerResult = parseResultSet(headerRs);
            List<Map<String, Object>> detailsResult = parseResultSet(detailRs);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", status);
            result.put("header", headerResult != null && !headerResult.isEmpty() ? headerResult.get(0) : null);
            result.put("chargeDetails", detailsResult);
            
            return result;
        } catch (Exception e) {
            log.error("Error loading quotation details for quoteId: {}", quoteId, e);
            return Map.of("error", "Failed to load quotation details: " + e.getMessage());
        }
    }

    /**
     * Loads and displays project related job details for control sheet
     * Stored Procedure: PROC_PROJECTS_JOBS_LOAD
     * 
     * @param jobId Job ID
     * @return Map containing status and job details
     */
    public Map<String, Object> loadProjectsJobs(Long jobId) {
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("PROC_PROJECTS_JOBS_LOAD");
            
            query.registerStoredProcedureParameter(1, Long.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(2, String.class, ParameterMode.OUT);
            query.registerStoredProcedureParameter(3, void.class, ParameterMode.REF_CURSOR);
            
            query.setParameter(1, jobId);
            query.execute();
            
            String status = (String) query.getOutputParameterValue(2);
            ResultSet rs = (ResultSet) query.getOutputParameterValue(3);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", status);
            result.put("jobs", parseResultSet(rs));
            
            return result;
        } catch (Exception e) {
            log.error("Error loading job details for jobId: {}", jobId, e);
            return Map.of("error", "Failed to load job details: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> parseResultSet(ResultSet rs) {
        List<Map<String, Object>> results = new ArrayList<>();
        if (rs == null) return results;

        try {
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    row.put(columnName, rs.getObject(i));
                }
                results.add(row);
            }
        } catch (Exception e) {
            log.error("Error parsing result set", e);
        }
        return results;
    }
}
