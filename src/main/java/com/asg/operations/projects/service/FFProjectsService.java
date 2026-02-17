package com.asg.operations.projects.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.operations.projects.dto.FFProjectsCtrlSheetDetailRequest;
import com.asg.operations.projects.dto.FFProjectsCtrlSheetDetailResponse;
import com.asg.operations.projects.dto.FFProjectsRequest;
import com.asg.operations.projects.dto.FFProjectsResponse;
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

    Map<String, Object> loadQuotationDetails(Long quotationPoid);

    Map<String, Object> loadJobDetails(Long transactionPoid);

    // Control Sheet operations
    List<FFProjectsCtrlSheetDetailResponse> batchControlSheetOperations(Long transactionPoid, List<FFProjectsCtrlSheetDetailRequest> requests);

    List<FFProjectsCtrlSheetDetailResponse> getControlSheetsByProject(Long transactionPoid, String freightType);

    // Projection methods for control sheet views
    List<?> getAirFreightJobs(Long transactionPoid);

    List<?> getSeaFreightJobs(Long transactionPoid);

    List<?> getRoadFreightJobs(Long transactionPoid);

    List<?> getAllFreightJobs(Long transactionPoid, LocalDate fromDate, LocalDate toDate);
}
