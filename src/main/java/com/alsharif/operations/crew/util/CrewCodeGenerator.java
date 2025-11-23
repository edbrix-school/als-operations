package com.alsharif.operations.crew.util;


import com.alsharif.operations.crew.repository.ContractCrewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Utility class for generating unique crew codes
 */
@Component
public class CrewCodeGenerator {

    @Autowired
    private ContractCrewRepository crewRepository;

    /**
     * Generate unique crew code
     * Format: CREW + sequence number (e.g., CREW001, CREW002)
     * 
     * Note: This is a simple implementation. You may need to adjust based on
     * your business rules or use a stored procedure as mentioned in the analysis document.
     */
    public String generateCrewCode(Long companyPoid, Long groupPoid) {
        // Try to get the next sequence number
        // This is a simplified approach - you might want to use a database sequence
        // or stored procedure for better concurrency handling
        
        String prefix = "CREW";
        int maxAttempts = 1000;
        
        for (int i = 1; i <= maxAttempts; i++) {
            String candidateCode = prefix + String.format("%06d", i);
            
            // Check if code already exists
//            if (!crewRepository.existsByCrewCode(candidateCode)) {
//                return candidateCode;
//            }
        }
        
        // Fallback to timestamp-based code if all attempts fail
        return prefix + System.currentTimeMillis();
    }

    /**
     * Alternative: Call stored procedure if available
     */
    public String generateCrewCodeFromProcedure(Long companyPoid, Long groupPoid) {
        // TODO: If using stored procedure PROC_GENERATE_CREW_CODE
        // Use @Procedure annotation or JDBC template to call it
        // This is a placeholder for stored procedure implementation
        
        // Example:
        // @Autowired
        // private JdbcTemplate jdbcTemplate;
        // 
        // SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
        //     .withProcedureName("PROC_GENERATE_CREW_CODE");
        // 
        // SqlParameterSource in = new MapSqlParameterSource()
        //     .addValue("COMPANY_POID", companyPoid)
        //     .addValue("GROUP_POID", groupPoid);
        // 
        // Map<String, Object> out = jdbcCall.execute(in);
        // return (String) out.get("CREW_CODE");
        
        return generateCrewCode(companyPoid, groupPoid); // Fallback to default method
    }
}

