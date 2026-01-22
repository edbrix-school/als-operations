package com.asg.operations.crew.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.operations.crew.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;

/**
 * Service interface for Contract Crew Master operations
 */
public interface ContractCrewService {
    /**
     * Get crew master by ID
     */
    ContractCrewResponse getCrewById(Long crewPoid);

    /**
     * Create new crew master
     */
    ContractCrewResponse createCrew(ContractCrewRequest request, Long companyPoid, Long groupPoid, String userId);

    /**
     * Update existing crew master
     */
    ContractCrewResponse updateCrew(Long companyPoid, String userId, Long crewPoid, ContractCrewRequest request);

    /**
     * Delete crew master (soft or hard delete)
     */
    void deleteCrew(Long companyPoid, Long crewPoid, @Valid DeleteReasonDto deleteReasonDto);

    /**
     * Get crew details list
     */
    CrewDetailsResponse getCrewDetails(Long companyPoid, Long crewPoid);

    /**
     * Bulk save crew details
     */
    CrewDetailsResponse saveCrewDetails(Long companyPoid, String userId, Long crewPoid, BulkSaveDetailsRequest request);

    /**
     * Delete single crew detail record
     */
    void deleteCrewDetail(Long companyPoid, Long crewPoid, Long detRowId);

    /**
     * Get all crew with filters
     */
    Map<String, Object> getAllCrewWithFilters(String documentId, FilterRequestDto filters, Pageable pageable, LocalDate periodFrom, LocalDate periodTo);
}

