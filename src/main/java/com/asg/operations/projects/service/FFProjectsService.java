package com.asg.operations.projects.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.operations.projects.dto.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface FFProjectsService {

    Map<String, Object> listProjectsWithFilters(String documentId, FilterRequestDto filterRequest, Pageable pageable, LocalDate periodFrom, LocalDate periodTo);

    FFProjectsResponse getProjectById(Long transactionPoid);

    FFProjectsResponse createProject(FFProjectsRequest request);

    FFProjectsResponse updateProject(Long transactionPoid, FFProjectsRequest request);

    void deleteProject(Long transactionPoid, DeleteReasonDto deleteReasonDto);

    Map<String, Object> loadQuotationDetails(Long quotationPoid, String quoteFlag);

    Map<String, Object> loadJobDetails(Long transactionPoid);

    // Control Sheet operations
    List<FFProjectsCtrlSheetDetailResponse> batchControlSheetOperations(Long transactionPoid, List<FFProjectsCtrlSheetDetailRequest> requests);

    List<FFProjectsCtrlSheetDetailResponse> getControlSheetsByProject(Long transactionPoid, String freightType);

    FFProjectsCtrlSheetDetailResponse createControlSheet(Long transactionPoid, FFProjectsCtrlSheetDetailRequest request);

    /**
     * Get comprehensive freight summary with all freight types
     */
    FreightJobsSummaryDTO getAllFreightsSummary(Long transactionPoid, FreightFilterRequest filter);

    /**
     * Get all freights list from control sheet
     */
    List<FreightSummaryDTO> getAllFreights(Long transactionPoid, LocalDate fromDate, LocalDate toDate, String sortBy, String sortDir);

    /**
     * Get job status/pending bills rows
     */
    List<JobStatusPendingBillDTO> getJobStatusPendingBills(Long transactionPoid, String viewBy, LocalDate fromDate, LocalDate toDate, String sortBy, String sortDir);

    /**
     * Get air freight jobs summary
     */
    List<AirFreightSummaryDTO> getAirFreightsSummary(Long transactionPoid, LocalDate fromDate, LocalDate toDate, String sortBy, String sortDir);

    /**
     * Get sea freight jobs summary
     */
    List<SeaFreightSummaryDTO> getSeaFreightsSummary(Long transactionPoid, LocalDate fromDate, LocalDate toDate, String sortBy, String sortDir);

    /**
     * Get road freight jobs summary
     */
    List<RoadFreightSummaryDTO> getRoadFreightsSummary(Long transactionPoid, LocalDate fromDate, LocalDate toDate, String sortBy, String sortDir);

    /**
     * Get detailed air freight information
     */
    AirFreightDetailedDTO getAirFreightDetails(Long projectId, Long jobId);

    /**
     * Get detailed sea freight information
     */
    SeaFreightDetailedDTO getSeaFreightDetails(Long projectId, Long jobId);

    /**
     * Get detailed road freight information
     */
    RoadFreightDetailedDTO getRoadFreightDetails(Long projectId, Long jobId);

    /**
     * Get upcoming jobs within date range
     */
    List<UpcomingJobDTO> getUpcomingJobsList(Long transactionPoid, LocalDate fromDate, LocalDate toDate, String sortBy, String sortDir);

    /**
     * Get job charges
     */
    JobChargesDTO getJobCharges(Long jobId);

    /**
     * Get all bayan details for a project
     */
    List<BayanDTO> getProjectBayanDetails(Long projectId, String sortBy, String sortDir);

    /**
     * Export control sheet to Excel
     */
    byte[] exportControlSheetToExcel(Long transactionPoid, String freightType, LocalDate fromDate, LocalDate toDate);

    /**
     * Export control sheet to PDF
     */
    byte[] exportControlSheetToPdf(Long transactionPoid, String freightType, LocalDate fromDate, LocalDate toDate);

    /**
     * Email control sheet
     */
    void emailControlSheet(Long transactionPoid, String emailAddress, String freightType, LocalDate fromDate, LocalDate toDate);

    /**
     * Create job from upcoming control sheet entry
     */
    Long createJobFromUpcoming(Long transactionPoid, Long controlSheetDetRowId);
}
