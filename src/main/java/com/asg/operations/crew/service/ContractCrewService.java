package com.asg.operations.crew.service;

import com.asg.operations.crew.dto.*;
import org.springframework.data.domain.Page;

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
    void deleteCrew(Long companyPoid, Long crewPoid);

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
    Page<ContractCrewListResponse> getAllCrewWithFilters(Long groupPoid, Long companyPoid, GetAllCrewFilterRequest filterRequest, int page, int size, String sort);
}

